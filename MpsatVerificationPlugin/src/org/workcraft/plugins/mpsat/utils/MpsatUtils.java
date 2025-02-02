package org.workcraft.plugins.mpsat.utils;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.*;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MpsatUtils {

    public static List<Solution> getCombinedChainSolutions(Result<? extends CombinedChainOutput> combinedChainResult) {
        LinkedList<Solution> solutions = null;
        if (combinedChainResult != null) {
            CombinedChainOutput combinedChainOutput = combinedChainResult.getPayload();
            if (combinedChainOutput != null) {
                if (combinedChainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    List<Result<? extends VerificationOutput>> mpsatResultList = combinedChainOutput.getMpsatResultList();
                    for (int index = 0; index < mpsatResultList.size(); ++index) {
                        Result<? extends VerificationOutput> mpsatResult = mpsatResultList.get(index);
                        if (mpsatResult != null) {
                            solutions.addAll(getSolutions(mpsatResult));
                        }
                    }
                } else if (combinedChainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = combinedChainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<Solution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            boolean isConsistencyCheck = false;
                            if (cause == Cause.INCONSISTENT) {
                                for (VerificationParameters mpsatSettings: combinedChainOutput.getMpsatSettingsList()) {
                                    if (mpsatSettings.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY) {
                                        isConsistencyCheck = true;
                                        break;
                                    }
                                }
                            }
                            if (isConsistencyCheck) {
                                solutions = new LinkedList<>();
                                solutions.add(punfOutcome.getFirst());
                            }
                        }
                    }
                }
            }
        }
        return solutions;
    }

    public static List<Solution> getChainSolutions(Result<? extends VerificationChainOutput> chainResult) {
        LinkedList<Solution> solutions = null;
        if (chainResult != null) {
            VerificationChainOutput chainOutput = chainResult.getPayload();
            if (chainOutput != null) {
                if (chainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    Result<? extends VerificationOutput> mpsatResult = chainOutput.getMpsatResult();
                    if (mpsatResult != null) {
                        solutions.addAll(getSolutions(mpsatResult));
                    }
                } else if (chainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = chainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<Solution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            VerificationParameters mpsatSettings = chainOutput.getMpsatSettings();
                            boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                                    && (mpsatSettings.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);
                            if (isConsistencyCheck) {
                                solutions = new LinkedList<>();
                                solutions.add(punfOutcome.getFirst());
                            }
                        }
                    }
                }
            }
        }
        return solutions;
    }

    public static List<Solution> getSolutions(Result<? extends VerificationOutput> result) {
        LinkedList<Solution> solutions = null;
        if ((result != null) && (result.getOutcome() == Outcome.SUCCESS)) {
            solutions = new LinkedList<>();
            VerificationOutput output = result.getPayload();
            if (output != null) {
                solutions.addAll(getSolutions(output));
            }
        }
        return solutions;
    }

    public static List<Solution> getSolutions(VerificationOutput output) {
        VerificationOutputParser mdp = new VerificationOutputParser(output);
        return mdp.getSolutions();
    }

    public static Boolean getCombinedChainOutcome(Result<? extends CombinedChainOutput> combinedChainResult) {
        List<Solution> solutions = getCombinedChainSolutions(combinedChainResult);
        if (solutions != null) {
            return !hasTraces(solutions);
        }
        return null;
    }

    public static Boolean getChainOutcome(Result<? extends VerificationChainOutput> chainResult) {
        List<Solution> solutions = getChainSolutions(chainResult);
        if (solutions != null) {
            return !hasTraces(solutions);
        }
        return null;
    }

    public static boolean hasTraces(List<Solution> solutions) {
        if (solutions != null) {
            for (Solution solution : solutions) {
                if ((solution.getMainTrace() != null) || (solution.getBranchTrace() != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static  String getToolchainDescription(String title) {
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }

    public static void playSolution(WorkspaceEntry we, Solution solution) {
        final Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            GraphEditorPanel editor = framework.getMainWindow().getEditor(we);
            final Toolbox toolbox = editor.getToolBox();
            final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
            toolbox.selectTool(tool);
            tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), editor);
            String comment = solution.getComment();
            if ((comment != null) && !comment.isEmpty()) {
                String traceText = solution.getMainTrace().toText();
                String message = comment.replaceAll("\\<.*?>", "") + " after trace: " + traceText;
                LogUtils.logWarning(message);
            }
        }
    }

    public static boolean mutexStructuralCheck(Stg stg, boolean allowEmptyMutexPlaces) {
        Collection<StgPlace> mutexPlaces = stg.getMutexPlaces();
        if (!allowEmptyMutexPlaces && mutexPlaces.isEmpty()) {
            DialogUtils.showWarning("No mutex places found to check implementability.");
            return false;
        }
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: mutexPlaces) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            String problematicPlacesString = ReferenceHelper.getNodesAsString(stg, problematicPlaces, SizeHelper.getWrapLength());
            DialogUtils.showError("A mutex place must precede two transitions of distinct\n" +
                    "output or internal signals, each with a single trigger.\n\n" +
                    "Problematic places are:" +
                    (problematicPlacesString.length() > SizeHelper.getWrapLength() - 20 ? "\n" : " ") +
                    problematicPlacesString);
            return false;
        }
        return true;
    }

    public static ArrayList<VerificationParameters> getMutexImplementabilitySettings(Collection<Mutex> mutexes) {
        final ArrayList<VerificationParameters> settingsList = new ArrayList<>();
        for (Mutex mutex: mutexes) {
            settingsList.add(VerificationParameters.getMutexImplementabilitySettings(mutex));
        }
        return settingsList;
    }

}
