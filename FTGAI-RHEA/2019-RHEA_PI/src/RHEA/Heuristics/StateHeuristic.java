package RHEA.Heuristics;

import RHEA.utils.GeneralInformation;
import struct.FrameData;

public abstract class StateHeuristic {

    abstract public double evaluateState(GeneralInformation gi);
}
