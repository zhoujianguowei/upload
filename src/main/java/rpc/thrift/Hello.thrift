include "./file/transfer/FileSegment.thrift"
namespace java rpc.thrift
service Hello{
    string helloString(1:string param);
    list<FileSegment.FileSegment> transferData(1: required FileSegment.FileSegment segment);

}
