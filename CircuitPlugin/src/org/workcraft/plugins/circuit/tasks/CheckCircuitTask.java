package org.workcraft.plugins.circuit.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class CheckCircuitTask extends MpsatChainTask {
	private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
			MpsatMode.UNDEFINED, 0, null, 0);

	private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
			MpsatMode.UNDEFINED, 0, null, 0);

	private final MpsatSettings deadlockSettings = new MpsatSettings("Deadlock",
			MpsatMode.DEADLOCK, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount());

	private final MpsatSettings hazardSettings = new MpsatSettings("Output persistency",
			MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount(), MpsatSettings.reachSemimodularity, true);

	private final WorkspaceEntry we;
	private final boolean checkConformation;
	private final boolean checkDeadlock;
	private final boolean checkHazard;

	public CheckCircuitTask(WorkspaceEntry we, boolean checkConformation, boolean checkDeadlock, boolean checkHazard) {
		super (we, null);
		this.we = we;
		this.checkConformation = checkConformation;
		this.checkDeadlock = checkDeadlock;
		this.checkHazard = checkHazard;
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		Framework framework = Framework.getInstance();
		File directory = null;
		try {
			// Common variables
			monitor.progressUpdate(0.05);
			VisualCircuit visualCircuit = (VisualCircuit)we.getModelEntry().getVisualModel();
			File envFile = visualCircuit.getEnvironmentFile();
			boolean hasEnvironment = ((envFile != null) && envFile.exists());

			String prefix = FileUtils.getTempPrefix(we.getTitle());
			directory = FileUtils.createTempDirectory(prefix);

			CircuitToStgConverter generator = new CircuitToStgConverter(visualCircuit);
			STG devStg = (STG)generator.getStg().getMathModel();
			String devStgName = (hasEnvironment ? StgUtils.DEVICE_FILE_NAME : StgUtils.SYSTEM_FILE_NAME) + StgUtils.ASTG_FILE_EXT;
			File devStgFile =  new File(directory, devStgName);
			Result<? extends Object> devExportResult = CircuitStgUtils.exportStg(devStg, devStgFile, directory, monitor);
			if (devExportResult.getOutcome() != Outcome.FINISHED) {
				if (devExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.10);

			// Generating .g for the environment
			STG sysStg;
			File sysStgFile = null;
			Result<? extends ExternalProcessResult>  pcompResult = null;
			if ( !hasEnvironment ) {
				 sysStgFile = devStgFile;
				 sysStg = devStg;
			} else {
				File envStgFile = null;
				if (envFile.getName().endsWith(StgUtils.ASTG_FILE_EXT)) {
					envStgFile = envFile;
				} else {
					STG envStg = (STG)framework.loadFile(envFile).getMathModel();
					envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_NAME + StgUtils.ASTG_FILE_EXT);
					Result<? extends Object> envExportResult = CircuitStgUtils.exportStg(envStg, envStgFile, directory, monitor);
					if (envExportResult.getOutcome() != Outcome.FINISHED) {
						if (envExportResult.getOutcome() == Outcome.CANCELLED) {
							return new Result<MpsatChainResult>(Outcome.CANCELLED);
						}
						return new Result<MpsatChainResult>(Outcome.FAILED,
								new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
					}
				}
				monitor.progressUpdate(0.20);

				// Generating .g for the whole system (circuit and environment)
				pcompResult = CircuitStgUtils.composeDevWithEnv(devStgFile, envStgFile, directory, monitor);
				if (pcompResult.getOutcome() != Outcome.FINISHED) {
					if (pcompResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
				}
				File compStgFile = new File(directory, StgUtils.COMPOSITION_FILE_NAME + StgUtils.ASTG_FILE_EXT);
				FileUtils.writeAllText(compStgFile, new String(pcompResult.getReturnValue().getOutput()));
				monitor.progressUpdate(0.25);

				// Restore input signals of the composed STG (these can be converted to outputs by PComp)
				sysStg = CircuitStgUtils.importStg(compStgFile);
				Set<String> inputSignalNames = devStg.getSignalNames(Type.INPUT, null);
				CircuitStgUtils.restoreInputSignals(sysStg, inputSignalNames);
				sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + StgUtils.ASTG_FILE_EXT);
				Result<? extends Object> sysExportResult = CircuitStgUtils.exportStg(sysStg, sysStgFile, directory, monitor);
				if (sysExportResult.getOutcome() != Outcome.FINISHED) {
					if (sysExportResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(sysExportResult, null, null, null, toolchainPreparationSettings));
				}
			}
			monitor.progressUpdate(0.30);

			// Generate unfolding
			File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_NAME + PunfUtilitySettings.getUnfoldingExtension(true));
			PunfTask punfTask = new PunfTask(sysStgFile.getCanonicalPath(), unfoldingFile.getCanonicalPath(), true);
			SubtaskMonitor<Object> punfMonitor = new SubtaskMonitor<Object>(monitor);
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
					punfTask, "Unfolding .g", punfMonitor);

			if (punfResult.getOutcome() != Outcome.FINISHED) {
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.40);

			// Check for interface conformation (only if the environment is specified)
			if (hasEnvironment && checkConformation) {
				Set<String> devOutputNames = devStg.getSignalFlatNames(Type.OUTPUT);
				Set<String> devPlaceNames = parsePlaceNames(pcompResult.getReturnValue().getOutputFile("places.list"), 0);
				String reachConformation = MpsatSettings.genReachConformation(devOutputNames, devPlaceNames);
				if (MpsatUtilitySettings.getDebugReach()) {
					System.out.println("\nReach expression for the interface conformation property:");
					System.out.println(reachConformation);
				}
				MpsatSettings conformationSettings = new MpsatSettings("Interface conformance",
						MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
						MpsatUtilitySettings.getSolutionCount(), reachConformation, true);

				MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
						unfoldingFile.getCanonicalPath(), directory, true);
				SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<Object>(monitor);
				Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
						mpsatConformationTask, "Running conformation check [MPSat]", mpsatMonitor);

				if (mpsatConformationResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatConformationResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
				}
				monitor.progressUpdate(0.50);

				MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
				if (!mpsatConformationParser.getSolutions().isEmpty()) {
					return new Result<MpsatChainResult>(Outcome.FINISHED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
									"Circuit does not conform to the environment after the following trace(s):"));
				}
			}
			monitor.progressUpdate(0.60);

			// Check for deadlock
			if (checkDeadlock) {
				MpsatTask mpsatDeadlockTask = new MpsatTask(deadlockSettings.getMpsatArguments(directory),
						unfoldingFile.getCanonicalPath(), directory, true);
				SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<Object>(monitor);
				Result<? extends ExternalProcessResult> mpsatDeadlockResult = framework.getTaskManager().execute(
						mpsatDeadlockTask, "Running deadlock check [MPSat]", mpsatMonitor);

				if (mpsatDeadlockResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatDeadlockResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings));
				}
				monitor.progressUpdate(0.70);

				MpsatResultParser mpsatDeadlockParser = new MpsatResultParser(mpsatDeadlockResult.getReturnValue());
				if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
					return new Result<MpsatChainResult>(Outcome.FINISHED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings,
									"Circuit has a deadlock after the following trace(s):"));
				}
			}
			monitor.progressUpdate(0.80);

			// Check for hazards
			if (checkHazard) {
				MpsatTask mpsatHazardTask = new MpsatTask(hazardSettings.getMpsatArguments(directory),
						unfoldingFile.getCanonicalPath(), directory, true);
				if (MpsatUtilitySettings.getDebugReach()) {
					System.out.println("\nReach expression for the hazard property:");
					System.out.println(hazardSettings.getReach());
				}
				SubtaskMonitor<Object> mpsatMonitor = new SubtaskMonitor<Object>(monitor);
				Result<? extends ExternalProcessResult>  mpsatHazardResult = framework.getTaskManager().execute(
						mpsatHazardTask, "Running hazard check [MPSat]", mpsatMonitor);

				if (mpsatHazardResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatHazardResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings));
				}
				monitor.progressUpdate(0.90);

				MpsatResultParser mpsatHazardParser = new MpsatResultParser(mpsatHazardResult.getReturnValue());
				if (!mpsatHazardParser.getSolutions().isEmpty()) {
					return new Result<MpsatChainResult>(Outcome.FINISHED,
							new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings,
									"Circuit has a hazard after the following trace(s):"));
				}
			}
			monitor.progressUpdate(1.0);

			// Success
			String message = getSuccessMessage(envFile);
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		} finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

	private HashSet<String> parsePlaceNames(byte[] bufferedInput, int lineIndex) {
		HashSet<String> result = new HashSet<String>();
		InputStream is = new ByteArrayInputStream(bufferedInput);
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line = null;
			while ((lineIndex >= 0) && ((line = br.readLine()) != null)) {
				lineIndex--;
			}
			if (line != null) {
				for (String name: line.trim().split("\\s")) {
					result.add(name);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getSuccessMessage(File environmentFile) {
		String message = "";
		boolean hasEnvironment = ((environmentFile != null) && environmentFile.exists());
		if (hasEnvironment) {
			message = "Under the given environment (" + environmentFile.getName() + ")";
		} else {
			message = "Without environment restrictions";
		}
		message +=  " the circuit is:\n";
		if (checkConformation) {
			message += "  * conformant\n";
		}
		if (checkDeadlock) {
			message += "  * deadlock-free\n";
		}
		if (checkHazard) {
			message += "  * hazard-free\n";
		}
		return message;
	}

}
