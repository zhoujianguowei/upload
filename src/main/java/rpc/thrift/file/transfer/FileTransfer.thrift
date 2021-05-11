include "FileSegment.thrift"
namespace java rpc.thrift.file.transfer
service FileTransfer{
	//transfer file,return current file process if it's a file;otherwise return -1 if a directory
	i64 transferFile(1:required FileSegment.FileSegment segment,2:optional string token);
}