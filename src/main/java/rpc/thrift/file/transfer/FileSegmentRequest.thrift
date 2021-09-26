namespace java rpc.thrift.file.transfer
//传输文件类型
enum FileTypeEnum{
	FILE_TYPE=0,
	DIR_TYPE=1
}
//三种加密方式，分别是java自带的MD5、BASE64以及自定义的算法
enum EncryptTypeEnum{
    MD5_TYPE=1,
    BASE64_TYPE=2,
    BYTE_REVERSE=3
}
//文件传输的thrift请求提
struct FileSegmentRequest{
	//文件或者目录的名称
	1: required string fileName;
	//保存的文件父目录，如果不存在，在服务端创建对应的目录
	2:optional string saveParentFolder;
	//相对路径，可选，如果设置会构造对应的路径形式
	3: optional string relativePath;
	//传输文件类型，文件或者目录
	4: required FileTypeEnum fileType;
	//当前传输的文件唯一标识，只有文件才有唯一标识信息
	5:required string identifier;
	//如果当前是文件，指的是当前文件传输的位置
	6:optional i64 startPos=0;
	//当前序列校验码
	7:required string checkSum;
	//传输的文件字节数组内容
	8:optional binary contents;
	//传输文件字节大小
	9:optional i32 bytesLength;
	//是否加密传输
	10:optional bool encrypted=false;
	//如果设置加密传输，加密传输的方式
	11:optional EncryptTypeEnum encryptedType;
	//文件总字节大小
	12:optional i64 totalFileLength;
	}

