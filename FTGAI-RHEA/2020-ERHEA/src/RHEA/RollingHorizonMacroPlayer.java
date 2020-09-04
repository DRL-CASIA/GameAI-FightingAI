package RHEA;

import java.util.Random;
import java.util.LinkedList;
import RHEA.utils.GeneralInformation;
import enumerate.Action;

/**
 * Created by rdgain on 6/28/2017.
 */
class RollingHorizonMacroPlayer extends RollingHorizonPlayer {

    // Macro actions
    private int m_actionsLeft;
    private int m_lastMacroAction;
    private boolean m_throwPop;

    RollingHorizonMacroPlayer(GeneralInformation gi, Random random) {
        super(gi, random);

        // Set up for macro actions
        m_actionsLeft = 0;
        m_lastMacroAction = -1;
        m_throwPop = true;
    }

    /**
     * Run the algorithm with macro action (expanding evolution over several game ticks, during which we execute
     * the same action several times)
     * @param stateObs - StateObservation of current game tick
     * @return - next action to play as integer
     */
    int run(GeneralInformation gi, RHEAAgent agent) {
        params = agent.getParameters();
        int budget = params.MAX_FM_CALLS;

        int nextAction;
        if (gi.getFrameData().getFramesNumber() == 0) {
            if (gi.startAvailActions.size() != start_nActions || m_throwPop)
                init(gi,agent);

            //Game just started, determine a macro-action.
            int best = evolve(gi, agent, budget);

            m_lastMacroAction = best;
            m_throwPop = true;
            nextAction = best;
            m_actionsLeft = params.MACRO_ACTION_LENGTH-1;

        } else {

            if(m_actionsLeft > 0) { //In the middle of the macro action.

                if (gi.startAvailActions.size() + 1 != start_nActions || m_throwPop)
                    init(gi,agent);
                prepareGameCopy(gi);

                evolve(gi, agent, budget);

                nextAction = m_lastMacroAction;
                m_actionsLeft--;
                m_throwPop = false;

            } else if(m_actionsLeft == 0) { //Finishing a macro-action
                prepareGameCopy(gi);

                int best = evolve(gi, agent, budget);
                nextAction = m_lastMacroAction;
                m_lastMacroAction = best;
                m_actionsLeft = params.MACRO_ACTION_LENGTH-1;
                m_throwPop = true;

            } else{
                throw new RuntimeException("This should not be happening: " + m_actionsLeft);
            }
        }
        return nextAction;
    }

    /**
     * Prepare the game copy for the macro action code
     * @param stateObs - current StateObservation
     */
    private void prepareGameCopy(GeneralInformation gi)
    {
        if(m_lastMacroAction != -1)
        {
            int first = params.MACRO_ACTION_LENGTH - m_actionsLeft - 1;
            for(int i = first; i < params.MACRO_ACTION_LENGTH; ++i)
            {
                if (!gi.IsGameOver()) {
                	System.out.println("Macro");
                	LinkedList<Action> oppActs = gi.oppStartHitActions;
                	Action oppAct = oppActs.get(random.nextInt(oppActs.size()));
                    gi.advanceOneStep(getStartActionMapping(m_lastMacroAction), oppAct);
                    numCalls++;
                } else break;
            }
        }
    }
}
