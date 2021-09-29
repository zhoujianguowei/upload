namespace java rpc.thrift.file.transfer
include "FileUploadRequest.thrift"
include "FileUploadResponse.thrift"
namespace java rpc.thrift.file.transfer
service FileTransferWorker{
	//upload file
	 FileUploadResponse.FileUploadResponse uploadFile(1:required FileUploadRequest.FileUploadRequest req,
	 2:optional string token);
}