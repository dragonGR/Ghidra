/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.app.util.bin.format.pdb2.pdbreader;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import ghidra.app.util.bin.format.pdb2.pdbreader.msf.MsfStream;
import ghidra.util.exception.CancelledException;

/**
 * Debug Data structures for PDB files.  There are a number of debug streams that can be processed.
 * <P>
 * NOTE: The processing that falls under DebugData is only partially done.  We have implemented
 *  and viewed the results of real data for:
 *  <LI> SECTION_HEADER.</LI>
 *  <P>
 *  We have partially implemented the following:
 *  <li> FRAME_POINTER_OMISSION</LI>
 *  <li> X_DATA</LI>
 *  <li> P_DATA</LI>
 */
public class DebugData {

	public enum DebugType {
		FRAME_POINTER_OMISSION(0),
		EXCEPTION(1),
		FIXUP(2),
		OMAP_TO_SOURCE(3),
		OMAP_FROM_SOURCE(4),
		SECTION_HEADER(5),
		TOKEN_RID_MAP(6),
		X_DATA(7),
		P_DATA(8),
		NEW_FRAME_POINTER_OMISSION(9),
		SECTION_HEADER_ORIG(10);

		private final int value;

		private DebugType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	//==============================================================================================
	// Internals
	//==============================================================================================
	private AbstractPdb pdb;
	private List<Integer> debugStreams = new ArrayList<>();

	private List<FramePointerOmissionRecord> framePointerOmissionData;
	// private SortedMap<Long, Long> omapToSource;
	private SortedMap<Long, Long> omapFromSource;
	private List<ImageSectionHeader> imageSectionHeaders;
	private List<ImageSectionHeader> imageSectionHeadersOrig;

	private List<ImageFunctionEntry> pData;

	private RvaVaDebugHeader xDataHeader;
	private PdbByteReader xDataReader;

	//==============================================================================================
	// API
	//==============================================================================================
	/**
	 * Constructor
	 * @param pdb {@link AbstractPdb} that owns this {@link DebugData}
	 */
	public DebugData(AbstractPdb pdb) {
		Objects.requireNonNull(pdb, "pdb cannot be null");
		this.pdb = pdb;
	}

	/**
	 * Returns the Frame Pointer Omission data
	 * @return the framePointerOmissionData or null if does not exist
	 */
	public List<FramePointerOmissionRecord> getFramePointerOmissionData() {
		return framePointerOmissionData;
	}

//	/**
//	 * Returns the OMAP_TO_SOURCE mapping of RVA to RVA
//	 * @return the omapToSource or null if does not exist.
//	 */
//	public SortedMap<Long, Long> getOmapToSource() {
//		return omapToSource;
//	}

	/**
	 * Returns the OMAP_FROM_SOURCE mapping of RVA to RVA
	 * @return the omapFromSource or null if does not exist.
	 */
	public SortedMap<Long, Long> getOmapFromSource() {
		return omapFromSource;
	}

	/**
	 * Returns the {@link List}&lt;{@link ImageSectionHeader}&gt;
	 * @return the imageSectionHeaders or null if does not exist
	 */
	public List<ImageSectionHeader> getImageSectionHeaders() {
		return imageSectionHeaders;
	}

	/**
	 * Returns the {@link List}&lt;{@link ImageSectionHeader}&gt;.
	 * When this returns a non-null list the OMAP_FROM_SRC should be
	 * used for remapping global symbols
	 * @return the imageSectionHeadersOrig or null if does not exist
	 */
	public List<ImageSectionHeader> getImageSectionHeadersOrig() {
		return imageSectionHeadersOrig;
	}

	/**
	 * Deserialize {@link DebugData} header from the {@link PdbByteReader} input.  This parses
	 *  stream numbers for varying Debug Types--the order/location of the stream number is for
	 *  each particular debug type (e.g., the first stream number read is for the stream containing
	 *  Frame Pointer Omission debug data).  A stream number of 0XFFFF (MsfStream.NIL_STREAM_NUMBER)
	 *  says that there is no data for that debug type; else the stream number represents the
	 *  stream that should be deserialized to retrieve the debug data of that type.  The
	 *  {@link #deserialize()} method deserializes each of these streams
	 *  that are valid to the corresponding debug data type
	 * @param reader {@link PdbByteReader} from which to parse the header
	 * @throws PdbException upon error in processing components
	 * @throws CancelledException upon user cancellation
	 */
	public void deserializeHeader(PdbByteReader reader)
			throws PdbException, CancelledException {
		while (reader.hasMore()) {
			pdb.checkCancelled();
			int debugStreamNumber = reader.parseUnsignedShortVal();
			debugStreams.add(debugStreamNumber);
		}
		if (debugStreams.size() != DebugType.values().length) {
			// TODO: implement something.
			//log.appendMsg("Unrecognized extra debug streams");
		}
	}

	/**
	 * Deserialize each valid {@link DebugData} stream, based upon valid stream numbers found while
	 *  parsing the {@link DebugData} header
	 * @throws PdbException PdbException upon error in processing components
	 * @throws CancelledException upon user cancellation
	 * @throws IOException on file seek or read, invalid parameters, bad file configuration, or
	 *  inability to read required bytes
	 */
	public void deserialize()
			throws PdbException, CancelledException, IOException {
		if (debugStreams.isEmpty()) {
			throw new PdbException(
				"DebugData Header had not been deserialized at the appropriate time");
		}
		for (DebugType dbg : DebugType.values()) {
			int streamNum = debugStreams.get(dbg.getValue());
			if (streamNum == MsfStream.NIL_STREAM_NUMBER) {
				continue;
			}
			switch (dbg) {
				case FRAME_POINTER_OMISSION:
					deserializeFramePointerOmissionData(streamNum);
					break;
				case EXCEPTION:
					// TODO: implement.
					break;
				case FIXUP:
					// TODO: implement.
					break;
				case OMAP_TO_SOURCE:
					// omapToSource = deserializeOMap(streamNum);
					break;
				case OMAP_FROM_SOURCE:
					omapFromSource = deserializeOMap(streamNum);
					break;
				case SECTION_HEADER:
					imageSectionHeaders = deserializeSectionHeaders(streamNum);
					break;
				case TOKEN_RID_MAP:
					// TODO: implement.
					break;
				case X_DATA:
					deserializeXData(streamNum);
					break;
				case P_DATA:
					deserializePData(streamNum);
					break;
				case NEW_FRAME_POINTER_OMISSION:
					// TODO: implement.
					break;
				case SECTION_HEADER_ORIG:
					imageSectionHeadersOrig = deserializeSectionHeaders(streamNum);
					break;
			}
		}
	}

	private void deserializeFramePointerOmissionData(int streamNum)
			throws PdbException, CancelledException, IOException {
		// TODO: check implementation for completeness.
		PdbByteReader reader = pdb.getReaderForStreamNumber(streamNum);
		framePointerOmissionData = new ArrayList<>();
		while (reader.hasMore()) {
			pdb.checkCancelled();
			FramePointerOmissionRecord framePointerOmissionRecord =
				new FramePointerOmissionRecord();
			framePointerOmissionRecord.parse(reader);
			framePointerOmissionData.add(framePointerOmissionRecord);
		}
	}

	private SortedMap<Long, Long> deserializeOMap(int streamNum)
			throws PdbException, CancelledException, IOException {
		PdbByteReader reader = pdb.getReaderForStreamNumber(streamNum);
		SortedMap<Long, Long> omap = new TreeMap<>();
		while (reader.hasMore()) {
			pdb.checkCancelled();
			long v1 = reader.parseUnsignedIntVal();
			long v2 = reader.parseUnsignedIntVal();
			omap.put(v1, v2);
		}
		return omap;
	}

	private List<ImageSectionHeader> deserializeSectionHeaders(int streamNum)
			throws PdbException, CancelledException, IOException {
		PdbByteReader reader = pdb.getReaderForStreamNumber(streamNum);
		List<ImageSectionHeader> sectionHeaders = new ArrayList<>();
		while (reader.hasMore()) {
			pdb.checkCancelled();
			ImageSectionHeader imageSectionHeader = new ImageSectionHeader(pdb);
			imageSectionHeader.parse(reader);
			sectionHeaders.add(imageSectionHeader);
		}
		return sectionHeaders;
	}

	// TODO: This is incomplete.
	/**
	 * See the {@link LinkerUnwindInfo} class that was built for and is pertinent to
	 *  processing XData
	 */
	private void deserializeXData(int streamNum)
			throws PdbException, CancelledException, IOException {
		PdbByteReader reader = pdb.getReaderForStreamNumber(streamNum);
		int streamLength = reader.getLimit();
		//System.out.println(reader.dump(0x20));
		RvaVaDebugHeader header = new RvaVaDebugHeader();
		xDataHeader = header;
		header.deserialize(reader);
		//System.out.println(header.dump());
		if (header.getHeaderVersion() != 1) {
			return; // Silent... TODO: add logging event.
		}
		long headerLength = header.getHeaderLength();
		long dataLength = header.getDataLength();
		if (headerLength + dataLength > streamLength) {
			throw new PdbException("Problem parsing Debug XData");
		}
		reader.setIndex((int) headerLength);
		//System.out.println(reader.dump());
		xDataReader = reader.getSubPdbByteReader(reader.numRemaining());
		// TODO: This is a partial implementation.  We need to figure out more to know
		//  how to deal with it.  The only API information regarding the XData is with
		//  regard to processing PData when the "machine" is IA64 or AMD64.  The interpretation
		//  for these machines is not real clear (or a bit of work), and there is no other
		//  interpretation available when the machine is different.
	}

	// TODO: This is incomplete.
	private void deserializePData(int streamNum)
			throws PdbException, CancelledException, IOException {
		PdbByteReader reader = pdb.getReaderForStreamNumber(streamNum);
		pData = new ArrayList<>();
		int streamLength = reader.getLimit();
		RvaVaDebugHeader header = new RvaVaDebugHeader();
		header.deserialize(reader);
		//System.out.println(header.dump());
		if (header.getHeaderVersion() != 1) {
			return; // Silent... TODO: add logging event.
		}
		long headerLength = header.getHeaderLength();
		long dataLength = header.getDataLength();
		if (headerLength + dataLength > streamLength) {
			throw new PdbException("Problem parsing Debug PData");
		}
		reader.setIndex((int) headerLength);
		//System.out.println(reader.dump());
		// TODO: current partial implementation does not work (throws exception)
		//  for ucrtbase.dll arm64.  Need to look at this closer.
//		while (reader.hasMore()) {
//			pdb.checkCancelled();
//			ImageFunctionEntry entry = new ImageFunctionEntry();
//			entry.deserialize(reader);
//			pData.add(entry);
//			long endPrologue = entry.getEndOfPrologueAddress();
//			// This is correct.  Using base from the XData header during this PData processing.
//			long base = xDataHeader.getRelativeVirtualAddressDataBase();
//			long index = endPrologue - base;
//			xDataReader.setIndex((int) index);
//			//System.out.println(xDataReader.dumpBytes(0x20));
//		}
		// TODO: More work possible.  See XData processing and notes there.  This is very
		//  incomplete.
		PdbDebugInfo debugInfo = pdb.getDebugInfo();
		if (debugInfo instanceof PdbNewDebugInfo) {
			//Processor target = pdb.getTargetProcessor();
			PdbNewDebugInfo dbi = (PdbNewDebugInfo) debugInfo;
			ImageFileMachine machine = dbi.getMachineType();
			switch (machine) {
				case IA64:
					break;
				case AMD64:
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Dumps the {@link DebugData}.  This package-protected method is for debugging only
	 * @param writer {@link Writer} to which to write the debug dump
	 * @throws IOException on issue writing to the {@link Writer}
	 * @throws CancelledException upon user cancellation
	 */
	void dump(Writer writer) throws IOException, CancelledException {
		writer.write("DebugData---------------------------------------------------\n");
		dumpDebugStreamList(writer);

		writer.write("FramePointerOmissionData------------------------------------\n");
		if (framePointerOmissionData != null) {
			for (FramePointerOmissionRecord framePointerOmissionRecord : framePointerOmissionData) {
				pdb.checkCancelled();
				framePointerOmissionRecord.dump(writer);
			}
		}
		writer.write("End FramePointerOmissionData--------------------------------\n");

//		writer.write("OmapToSource------------------------------------------------\n");
//		if (omapToSource != null) {
//			int num = 0;
//			for (Map.Entry<Long, Long> entry : omapToSource.entrySet()) {
//				pdb.checkCancelled();
//				writer.write(String.format("0X%08X: 0X%012X,  0X%012X\n", num++, entry.getKey(),
//					entry.getValue()));
//			}
//		}
//		writer.write("End OmapToSource--------------------------------------------\n");
//
		writer.write("OmapFromSource----------------------------------------------\n");
		if (omapFromSource != null) {
			int num = 0;
			for (Map.Entry<Long, Long> entry : omapFromSource.entrySet()) {
				pdb.checkCancelled();
				writer.write(String.format("0X%08X: 0X%012X,  0X%012X\n", num++, entry.getKey(),
					entry.getValue()));
			}
		}
		writer.write("End OmapFromSource------------------------------------------\n");

		writer.write("ImageSectionHeaders-----------------------------------------\n");
		if (imageSectionHeaders != null) {
			int sectionNum = 0;
			for (ImageSectionHeader imageSectionHeader : imageSectionHeaders) {
				pdb.checkCancelled();
				imageSectionHeader.dump(writer, sectionNum++);
			}
		}
		writer.write("End ImageSectionHeaders-------------------------------------\n");

		writer.write("ImageSectionHeadersOrig-------------------------------------\n");
		if (imageSectionHeadersOrig != null) {
			int sectionNum = 0;
			for (ImageSectionHeader imageSectionHeader : imageSectionHeadersOrig) {
				pdb.checkCancelled();
				imageSectionHeader.dump(writer, sectionNum++);
			}
		}
		writer.write("End ImageSectionHeadersOrig---------------------------------\n");

		writer.write("PData-------------------------------------------------------\n");
		if (pData != null) {
			for (ImageFunctionEntry entry : pData) {
				pdb.checkCancelled();
				// TODO: need to output more if/when more PData is available (e.g., interpretation
				//  of XData.
				writer.append(entry.toString());
			}
		}
		writer.write("End PData---------------------------------------------------\n");

		writer.write("End DebugData-----------------------------------------------\n");
	}

	/**
	 * Dumps the DebugStreamList.  This package-protected method is for debugging only
	 * @param writer {@link Writer} to which to write the debug dump
	 * @throws IOException on issue writing to the {@link Writer}
	 * @throws CancelledException upon user cancellation
	 */
	private void dumpDebugStreamList(Writer writer) throws IOException, CancelledException {
		writer.write("StreamList--------------------------------------------------\n");
		int i = 0;
		for (int strmNumber : debugStreams) {
			pdb.checkCancelled();
			writer.write(String.format("StrmNumber[%02d]: %04x\n", i++, strmNumber));
		}
		writer.write("End StreamList----------------------------------------------\n");
	}

}
