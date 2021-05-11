namespace java rpc.thrift.file.transfer
enum FileTypeEnum{
	FILE_TYPE,
	DIR_TYPE
}
struct FileSegment{
	//file or directory name
	1: required string fileName;
	3: optional string relativePath;
	//file type 0 indicate file,1 indicate directory
	4: required FileTypeEnum fileType=FileTypeEnum.FILE_TYPE;
	//file identifier,note uniq file identifier,only for file type
	5:required string identifier;
	//seek position
	6:required i64 pos=0;
	//checkSum for check contents info
	7:required string checkSum;
	//file contents
	8:required binary contents;
	//write bytes number
	9:required i32 bytesLength;
}

