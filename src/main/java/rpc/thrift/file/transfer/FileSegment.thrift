namespace java rpc.thrift.file.transfer
struct FileSegment{
	1: string fileName;
	2: required binary contents;
	3: optional list<string> userNameList;
}
