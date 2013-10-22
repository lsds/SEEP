package uk.ac.imperial.lsds.seep.comm.serialization;

import java.util.ArrayList;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.DistributedScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateAck;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupOperatorState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.KeyBounds;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.BackupRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Ack;
import uk.ac.imperial.lsds.seep.elastic.MockState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.RawData;
import uk.ac.imperial.lsds.seep.reliable.MemoryChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ScaleOutInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitRI;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReconfigureConnection;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InvalidateState;
import junit.framework.*;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StreamState;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.OpenSignal;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.InitOperatorState;
import uk.ac.imperial.lsds.seep.runtimeengine.CoreRE;
import uk.ac.imperial.lsds.seep.operator.State;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.StateChunk;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.ReplayStateInfo;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.Resume;
import uk.ac.imperial.lsds.seep.comm.serialization.controlhelpers.CloseSignal;

/**
 * The class <code>ControlTupleTest</code> contains tests for the class <code>{@link ControlTuple}</code>.
 *
 * @generatedBy CodePro at 18/10/13 19:13
 * @author rc3011
 * @version $Revision: 1.0 $
 */
public class ControlTupleTest extends TestCase {
	/**
	 * Run the ControlTuple() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testControlTuple_1()
		throws Exception {

		ControlTuple result = new ControlTuple();

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.null", result.toString());
		assertEquals(null, result.getType());
		assertEquals(null, result.getBackupRI());
		assertEquals(null, result.getInitOperatorState());
		assertEquals(null, result.getBackupState());
		assertEquals(null, result.getInitRI());
		assertEquals(null, result.getOpenSignal());
		assertEquals(null, result.getInvalidateState());
		assertEquals(null, result.getRawData());
		assertEquals(null, result.getScaleOutInfo());
		assertEquals(null, result.getStateChunk());
		assertEquals(null, result.getKeyBounds());
		assertEquals(null, result.getReconfigureConnection());
		assertEquals(null, result.getStreamState());
		assertEquals(null, result.getStateAck());
		assertEquals(null, result.getAck());
		assertEquals(null, result.getDistributedScaleOutInfo());
		assertEquals(null, result.getCloseSignal());
		assertEquals(null, result.getReplayStateInfo());
		assertEquals(null, result.getResume());
	}

	/**
	 * Run the ControlTuple(ControlTupleType,int) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testControlTuple_2()
		throws Exception {
		uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType type = uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK;
		int opId = 1;

		ControlTuple result = new ControlTuple(type, opId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.ACK", result.toString());
		assertEquals(null, result.getBackupRI());
		assertEquals(null, result.getInitOperatorState());
		assertEquals(null, result.getBackupState());
		assertEquals(null, result.getInitRI());
		assertEquals(null, result.getOpenSignal());
		assertEquals(null, result.getRawData());
		assertEquals(null, result.getScaleOutInfo());
		assertEquals(null, result.getStateChunk());
		assertEquals(null, result.getKeyBounds());
		assertEquals(null, result.getReconfigureConnection());
		assertEquals(null, result.getStreamState());
		assertEquals(null, result.getStateAck());
		assertEquals(null, result.getAck());
		assertEquals(null, result.getDistributedScaleOutInfo());
		assertEquals(null, result.getCloseSignal());
		assertEquals(null, result.getReplayStateInfo());
		assertEquals(null, result.getResume());
	}

	/**
	 * Run the ControlTuple(ControlTupleType,int,long) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testControlTuple_3()
		throws Exception {
		uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType type = uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK;
		int opId = 1;
		long ts = 1L;

		ControlTuple result = new ControlTuple(type, opId, ts);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.ACK", result.toString());
		assertEquals(null, result.getBackupRI());
		assertEquals(null, result.getInitOperatorState());
		assertEquals(null, result.getBackupState());
		assertEquals(null, result.getInitRI());
		assertEquals(null, result.getOpenSignal());
		assertEquals(null, result.getInvalidateState());
		assertEquals(null, result.getRawData());
		assertEquals(null, result.getScaleOutInfo());
		assertEquals(null, result.getStateChunk());
		assertEquals(null, result.getKeyBounds());
		assertEquals(null, result.getReconfigureConnection());
		assertEquals(null, result.getStreamState());
		assertEquals(null, result.getStateAck());
		assertEquals(null, result.getDistributedScaleOutInfo());
		assertEquals(null, result.getCloseSignal());
		assertEquals(null, result.getReplayStateInfo());
		assertEquals(null, result.getResume());
	}

	/**
	 * Run the Ack getAck() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		Ack result = fixture.getAck();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0L, result.getTs());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the BackupRI getBackupRI() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetBackupRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		BackupRI result = fixture.getBackupRI();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getKey());
		assertEquals(null, result.getIndex());
		assertEquals(null, result.getOperatorType());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the BackupOperatorState getBackupState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetBackupState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		BackupOperatorState result = fixture.getBackupState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getState());
		assertEquals(null, result.getStateClass());
		assertEquals(null, result.getOutputBuffers());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the CloseSignal getCloseSignal() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetCloseSignal_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		CloseSignal result = fixture.getCloseSignal();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getTotalNumberOfChunks());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the DistributedScaleOutInfo getDistributedScaleOutInfo() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetDistributedScaleOutInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		DistributedScaleOutInfo result = fixture.getDistributedScaleOutInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getNewOpId());
		assertEquals(0, result.getOldOpId());
	}

	/**
	 * Run the InitOperatorState getInitOperatorState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetInitOperatorState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		InitOperatorState result = fixture.getInitOperatorState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getState());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the InitRI getInitRI() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetInitRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		InitRI result = fixture.getInitRI();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getKey());
		assertEquals(null, result.getIndex());
		assertEquals(0, result.getNodeId());
	}

	/**
	 * Run the InvalidateState getInvalidateState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetInvalidateState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		InvalidateState result = fixture.getInvalidateState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getOperatorId());
	}

	/**
	 * Run the KeyBounds getKeyBounds() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetKeyBounds_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		KeyBounds result = fixture.getKeyBounds();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getMaxBound());
		assertEquals(0, result.getMinBound());
	}

	/**
	 * Run the OpenSignal getOpenSignal() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetOpenSignal_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		OpenSignal result = fixture.getOpenSignal();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the RawData getRawData() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetRawData_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		RawData result = fixture.getRawData();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getData());
		assertEquals(null, result.getTs());
		assertEquals(0, result.getOpId());
	}

	/**
	 * Run the ReconfigureConnection getReconfigureConnection() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetReconfigureConnection_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		ReconfigureConnection result = fixture.getReconfigureConnection();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getInC());
		assertEquals(0, result.getNode_port());
		assertEquals(false, result.getOperatorNature());
		assertEquals(0, result.getInD());
		assertEquals(null, result.getOperatorType());
		assertEquals(0, result.getOriginalOpId());
		assertEquals(null, result.getIp());
		assertEquals(0, result.getOpId());
		assertEquals(null, result.getCommand());
	}

	/**
	 * Run the ReplayStateInfo getReplayStateInfo() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetReplayStateInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		ReplayStateInfo result = fixture.getReplayStateInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isStreamToSingleNode());
		assertEquals(0, result.getNewOpId());
		assertEquals(0, result.getOldOpId());
	}

	/**
	 * Run the Resume getResume() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetResume_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		Resume result = fixture.getResume();

		// add additional test code here
		assertNotNull(result);
		assertEquals(null, result.getOpId());
	}

	/**
	 * Run the ScaleOutInfo getScaleOutInfo() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetScaleOutInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		ScaleOutInfo result = fixture.getScaleOutInfo();

		// add additional test code here
		assertNotNull(result);
		assertEquals(false, result.isStatefulScaleOut());
		assertEquals(0, result.getNewOpId());
		assertEquals(0, result.getOldOpId());
	}

	/**
	 * Run the StateAck getStateAck() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetStateAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		StateAck result = fixture.getStateAck();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getMostUpstreamOpId());
		assertEquals(0, result.getNodeId());
	}

	/**
	 * Run the StateChunk getStateChunk() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetStateChunk_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		StateChunk result = fixture.getStateChunk();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getKeeperOpId());
		assertEquals(0, result.getTotalChunks());
		assertEquals(0, result.getOwnerOpId());
		assertEquals(null, result.getMemoryChunk());
		assertEquals(0, result.getSplittingKey());
		assertEquals(0, result.getSequenceNumber());
	}

	/**
	 * Run the StreamState getStreamState() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetStreamState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		StreamState result = fixture.getStreamState();

		// add additional test code here
		assertNotNull(result);
		assertEquals(0, result.getTargetOpId());
	}

	/**
	 * Run the uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType getType() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testGetType_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType result = fixture.getType();

		// add additional test code here
		assertNotNull(result);
		assertEquals("BACKUP_OP_STATE", result.name());
		assertEquals("BACKUP_OP_STATE", result.toString());
		assertEquals(1, result.ordinal());
	}

	/**
	 * Run the ControlTuple makeBackupRI(int,ArrayList<Integer>,ArrayList<Integer>,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeBackupRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;
		ArrayList<Integer> indexes = new ArrayList();
		ArrayList<Integer> keys = new ArrayList();
		String operatorType = "";

		ControlTuple result = fixture.makeBackupRI(opId, indexes, keys, operatorType);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.BACKUP_RI", result.toString());
	}

	/**
	 * Run the ControlTuple makeBackupState(BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeBackupState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		BackupOperatorState bs = new BackupOperatorState();

		ControlTuple result = fixture.makeBackupState(bs);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.BACKUP_OP_STATE", result.toString());
	}

	/**
	 * Run the ControlTuple makeCloseSignalBackup(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeCloseSignalBackup_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;
		int totalNumberOfChunks = 1;

		ControlTuple result = fixture.makeCloseSignalBackup(opId, totalNumberOfChunks);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.CLOSE_BACKUP_SIGNAL", result.toString());
	}

	/**
	 * Run the ControlTuple makeDistributedScaleOut(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeDistributedScaleOut_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opIdToParallelize = 1;
		int newOpId = 1;

		ControlTuple result = fixture.makeDistributedScaleOut(opIdToParallelize, newOpId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.DISTRIBUTED_SCALE_OUT", result.toString());
	}

	/**
	 * Run the ControlTuple makeGenericAck(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeGenericAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int nodeId = 1;

		ControlTuple result = fixture.makeGenericAck(nodeId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.ACK", result.toString());
	}

	/**
	 * Run the ControlTuple makeInitOperatorState(int,State) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeInitOperatorState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int senderOperatorId = 1;
		State initOperatorState = new MockState();

		ControlTuple result = fixture.makeInitOperatorState(senderOperatorId, initOperatorState);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.INIT_STATE", result.toString());
	}

	/**
	 * Run the ControlTuple makeInitRI(int,ArrayList<Integer>,ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeInitRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int nodeId = 1;
		ArrayList<Integer> indexes = new ArrayList();
		ArrayList<Integer> keys = new ArrayList();

		ControlTuple result = fixture.makeInitRI(nodeId, indexes, keys);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.INIT_RI", result.toString());
	}

	/**
	 * Run the ControlTuple makeInvalidateMessage(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeInvalidateMessage_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opIdToInvalidate = 1;

		ControlTuple result = fixture.makeInvalidateMessage(opIdToInvalidate);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.INVALIDATE_STATE", result.toString());
	}

	/**
	 * Run the ControlTuple makeKeyBounds(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeKeyBounds_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int minBound = 1;
		int maxBound = 1;

		ControlTuple result = fixture.makeKeyBounds(minBound, maxBound);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.KEY_SPACE_BOUNDS", result.toString());
	}

	/**
	 * Run the ControlTuple makeOpenSignalBackup(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeOpenSignalBackup_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;

		ControlTuple result = fixture.makeOpenSignalBackup(opId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.OPEN_BACKUP_SIGNAL", result.toString());
	}

	/**
	 * Run the ControlTuple makeReconfigure(int,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeReconfigure_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;
		String command = "";
		String ip = "";

		ControlTuple result = fixture.makeReconfigure(opId, command, ip);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.RECONFIGURE", result.toString());
	}

	/**
	 * Run the ControlTuple makeReconfigure(int,int,String,String,int,int,int,boolean,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeReconfigure_2()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;
		int originalOpId = 1;
		String command = "";
		String ip = "";
		int nodePort = 1;
		int inC = 1;
		int inD = 1;
		boolean operatorNature = true;
		String operatorType = "";

		ControlTuple result = fixture.makeReconfigure(opId, originalOpId, command, ip, nodePort, inC, inD, operatorNature, operatorType);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.RECONFIGURE", result.toString());
	}

	/**
	 * Run the ControlTuple makeReconfigureSingleCommand(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeReconfigureSingleCommand_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		String command = "";

		ControlTuple result = fixture.makeReconfigureSingleCommand(command);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.RECONFIGURE", result.toString());
	}

	/**
	 * Run the ControlTuple makeReconfigureSourceRate(int,String,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeReconfigureSourceRate_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opId = 1;
		String command = "";
		int inC = 1;

		ControlTuple result = fixture.makeReconfigureSourceRate(opId, command, inC);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.RECONFIGURE", result.toString());
	}

	/**
	 * Run the ControlTuple makeReplayStateInfo(int,int,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeReplayStateInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int oldOpId = 1;
		int newOpId = 1;
		boolean singleNode = true;

		ControlTuple result = fixture.makeReplayStateInfo(oldOpId, newOpId, singleNode);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.STREAM_STATE", result.toString());
	}

	/**
	 * Run the ControlTuple makeResume(ArrayList<Integer>) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeResume_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		ArrayList<Integer> opIds = new ArrayList();

		ControlTuple result = fixture.makeResume(opIds);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.RESUME", result.toString());
	}

	/**
	 * Run the ControlTuple makeScaleOut(int,int,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeScaleOut_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int opIdToParallelize = 1;
		int newOpId = 1;
		boolean isStateful = true;

		ControlTuple result = fixture.makeScaleOut(opIdToParallelize, newOpId, isStateful);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.SCALE_OUT", result.toString());
	}

	/**
	 * Run the ControlTuple makeStateAck(int,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeStateAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int nodeId = 1;
		int mostUpstreamOpId = 1;

		ControlTuple result = fixture.makeStateAck(nodeId, mostUpstreamOpId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.STATE_ACK", result.toString());
	}

	/**
	 * Run the ControlTuple makeStateChunk(int,int,int,int,MemoryChunk,int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeStateChunk_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int ownerOpId = 1;
		int keeperOpId = 1;
		int seqNumber = 1;
		int totalChunks = 1;
		MemoryChunk mc = new MemoryChunk();
		int splittingKey = 1;

		ControlTuple result = fixture.makeStateChunk(ownerOpId, keeperOpId, seqNumber, totalChunks, mc, splittingKey);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.STATE_CHUNK", result.toString());
	}

	/**
	 * Run the ControlTuple makeStreamState(int) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testMakeStreamState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		int targetOpId = 1;

		ControlTuple result = fixture.makeStreamState(targetOpId);

		// add additional test code here
		assertNotNull(result);
		assertEquals("ControlTuple.STREAM_STATE", result.toString());
	}

	/**
	 * Run the void setAck(Ack) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		Ack ack = new Ack();

		fixture.setAck(ack);

		// add additional test code here
	}

	/**
	 * Run the void setBackupRI(BackupRI) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetBackupRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		BackupRI backupRI = new BackupRI();

		fixture.setBackupRI(backupRI);

		// add additional test code here
	}

	/**
	 * Run the void setBackupState(BackupOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetBackupState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		BackupOperatorState backupState = new BackupOperatorState();

		fixture.setBackupState(backupState);

		// add additional test code here
	}

	/**
	 * Run the void setCloseSignal(CloseSignal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetCloseSignal_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		CloseSignal closeSignal = new CloseSignal();

		fixture.setCloseSignal(closeSignal);

		// add additional test code here
	}

	/**
	 * Run the void setDistributedScaleOutInfo(DistributedScaleOutInfo) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetDistributedScaleOutInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		DistributedScaleOutInfo distributedScaleOutInfo = new DistributedScaleOutInfo();

		fixture.setDistributedScaleOutInfo(distributedScaleOutInfo);

		// add additional test code here
	}

	/**
	 * Run the void setInitOperatorState(InitOperatorState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetInitOperatorState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		InitOperatorState initOperatorState = new InitOperatorState();

		fixture.setInitOperatorState(initOperatorState);

		// add additional test code here
	}

	/**
	 * Run the void setInitRI(InitRI) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetInitRI_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		InitRI initRI = new InitRI();

		fixture.setInitRI(initRI);

		// add additional test code here
	}

	/**
	 * Run the void setInvalidateState(InvalidateState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetInvalidateState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		InvalidateState invalidateState = new InvalidateState();

		fixture.setInvalidateState(invalidateState);

		// add additional test code here
	}

	/**
	 * Run the void setKeyBounds(KeyBounds) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetKeyBounds_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		KeyBounds keyBounds = new KeyBounds();

		fixture.setKeyBounds(keyBounds);

		// add additional test code here
	}

	/**
	 * Run the void setOpenSignal(OpenSignal) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetOpenSignal_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		OpenSignal openSignal = new OpenSignal();

		fixture.setOpenSignal(openSignal);

		// add additional test code here
	}

	/**
	 * Run the void setRawData(RawData) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetRawData_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		RawData rawData = new RawData();

		fixture.setRawData(rawData);

		// add additional test code here
	}

	/**
	 * Run the void setReconfigureConnection(ReconfigureConnection) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetReconfigureConnection_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		ReconfigureConnection reconfigureConnection = new ReconfigureConnection();

		fixture.setReconfigureConnection(reconfigureConnection);

		// add additional test code here
	}

	/**
	 * Run the void setReplayStateInfo(ReplayStateInfo) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetReplayStateInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		ReplayStateInfo replayStateInfo = new ReplayStateInfo();

		fixture.setReplayStateInfo(replayStateInfo);

		// add additional test code here
	}

	/**
	 * Run the void setResume(Resume) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetResume_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		Resume resume = new Resume();

		fixture.setResume(resume);

		// add additional test code here
	}

	/**
	 * Run the void setScaleOutInfo(ScaleOutInfo) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetScaleOutInfo_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		ScaleOutInfo scaleOutInfo = new ScaleOutInfo();

		fixture.setScaleOutInfo(scaleOutInfo);

		// add additional test code here
	}

	/**
	 * Run the void setStateAck(StateAck) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetStateAck_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		StateAck stateAck = new StateAck();

		fixture.setStateAck(stateAck);

		// add additional test code here
	}

	/**
	 * Run the void setStateChunk(StateChunk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetStateChunk_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		StateChunk stateChunk = new StateChunk();

		fixture.setStateChunk(stateChunk);

		// add additional test code here
	}

	/**
	 * Run the void setStreamState(StreamState) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetStreamState_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		StreamState streamState = new StreamState();

		fixture.setStreamState(streamState);

		// add additional test code here
	}

	/**
	 * Run the void setType(ControlTupleType) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testSetType_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());
		uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType type = uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK;

		fixture.setType(type);

		// add additional test code here
	}

	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public void testToString_1()
		throws Exception {
		ControlTuple fixture = new ControlTuple(uk.ac.imperial.lsds.seep.runtimeengine.CoreRE.ControlTupleType.ACK, 1);
		fixture.makeBackupState(new BackupOperatorState());
		fixture.setResume(new Resume());
		fixture.setStateChunk(new StateChunk());
		fixture.setOpenSignal(new OpenSignal());
		fixture.setReconfigureConnection(new ReconfigureConnection());
		fixture.setInitOperatorState(new InitOperatorState());
		fixture.setDistributedScaleOutInfo(new DistributedScaleOutInfo());
		fixture.setStateAck(new StateAck());
		fixture.setRawData(new RawData());
		fixture.setScaleOutInfo(new ScaleOutInfo());
		fixture.setAck(new Ack());
		fixture.setKeyBounds(new KeyBounds());
		fixture.setCloseSignal(new CloseSignal());
		fixture.setStreamState(new StreamState());
		fixture.setInvalidateState(new InvalidateState());
		fixture.setBackupRI(new BackupRI());
		fixture.setReplayStateInfo(new ReplayStateInfo());
		fixture.setInitRI(new InitRI());

		String result = fixture.toString();

		// add additional test code here
		assertEquals("ControlTuple.BACKUP_OP_STATE", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @see TestCase#setUp()
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	protected void setUp()
		throws Exception {
		super.setUp();
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @see TestCase#tearDown()
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	protected void tearDown()
		throws Exception {
		super.tearDown();
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 18/10/13 19:13
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// Run all of the tests
			junit.textui.TestRunner.run(ControlTupleTest.class);
		} else {
			// Run only the named tests
			TestSuite suite = new TestSuite("Selected tests");
			for (int i = 0; i < args.length; i++) {
				TestCase test = new ControlTupleTest();
				test.setName(args[i]);
				suite.addTest(test);
			}
			junit.textui.TestRunner.run(suite);
		}
	}
}