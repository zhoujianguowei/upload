namespace java rpc.thrift.file.transfer
include "FileSegmentRequest.thrift"
include "FileSegmentResponse.thrift"
namespace java rpc.thrift.file.transfer
service FileTransferWorker{
	//upload file
	 FileSegmentResponse.FileSegmentResponse transferFile(1:required FileSegmentRequest.FileSegmentRequest req,
	 2:optional string token);
}