package uk.ac.imperial.lsds.seep.operator;

import uk.ac.imperial.lsds.seep.operator.compose.multi.MultiOpTuple;

public interface MultiAPI extends API {

	public void send(MultiOpTuple dt);

}
