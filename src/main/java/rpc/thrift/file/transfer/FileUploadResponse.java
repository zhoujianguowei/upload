/**
 * Autogenerated by Thrift Compiler (0.15.0)
 * <p>
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *
 * @generated
 */
package rpc.thrift.file.transfer;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.15.0)", date = "2021-10-17")
public class FileUploadResponse implements org.apache.thrift.TBase<FileUploadResponse, FileUploadResponse._Fields>, java.io.Serializable, Cloneable, Comparable<FileUploadResponse> {
    private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("FileUploadResponse");

    private static final org.apache.thrift.protocol.TField UPLOAD_STATUS_RESULT_FIELD_DESC = new org.apache.thrift.protocol.TField("uploadStatusResult", org.apache.thrift.protocol.TType.I32, (short) 1);
    private static final org.apache.thrift.protocol.TField NEXT_POS_FIELD_DESC = new org.apache.thrift.protocol.TField("nextPos", org.apache.thrift.protocol.TType.I64, (short) 2);
    private static final org.apache.thrift.protocol.TField ERROR_MSG_FIELD_DESC = new org.apache.thrift.protocol.TField("errorMsg", org.apache.thrift.protocol.TType.STRING, (short) 3);

    private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new FileUploadResponseStandardSchemeFactory();
    private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new FileUploadResponseTupleSchemeFactory();

    /**
     * @see ResResult
     */
    public @org.apache.thrift.annotation.Nullable
    ResResult uploadStatusResult; // required
    public long nextPos; // optional
    public @org.apache.thrift.annotation.Nullable
    java.lang.String errorMsg; // optional

    /**
     * The set of fields this struct contains, along with convenience methods for finding and manipulating them.
     */
    public enum _Fields implements org.apache.thrift.TFieldIdEnum {
        /**
         * @see ResResult
         */
        UPLOAD_STATUS_RESULT((short) 1, "uploadStatusResult"),
        NEXT_POS((short) 2, "nextPos"),
        ERROR_MSG((short) 3, "errorMsg");

        private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

        static {
            for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
                byName.put(field.getFieldName(), field);
            }
        }

        /**
         * Find the _Fields constant that matches fieldId, or null if its not found.
         */
        @org.apache.thrift.annotation.Nullable
        public static _Fields findByThriftId(int fieldId) {
            switch (fieldId) {
                case 1: // UPLOAD_STATUS_RESULT
                    return UPLOAD_STATUS_RESULT;
                case 2: // NEXT_POS
                    return NEXT_POS;
                case 3: // ERROR_MSG
                    return ERROR_MSG;
                default:
                    return null;
            }
        }

        /**
         * Find the _Fields constant that matches fieldId, throwing an exception
         * if it is not found.
         */
        public static _Fields findByThriftIdOrThrow(int fieldId) {
            _Fields fields = findByThriftId(fieldId);
            if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
            return fields;
        }

        /**
         * Find the _Fields constant that matches name, or null if its not found.
         */
        @org.apache.thrift.annotation.Nullable
        public static _Fields findByName(java.lang.String name) {
            return byName.get(name);
        }

        private final short _thriftId;
        private final java.lang.String _fieldName;

        _Fields(short thriftId, java.lang.String fieldName) {
            _thriftId = thriftId;
            _fieldName = fieldName;
        }

        public short getThriftFieldId() {
            return _thriftId;
        }

        public java.lang.String getFieldName() {
            return _fieldName;
        }
    }

    // isset id assignments
    private static final int __NEXTPOS_ISSET_ID = 0;
    private byte __isset_bitfield = 0;
    private static final _Fields optionals[] = {_Fields.NEXT_POS, _Fields.ERROR_MSG};
    public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;

    static {
        java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
        tmpMap.put(_Fields.UPLOAD_STATUS_RESULT, new org.apache.thrift.meta_data.FieldMetaData("uploadStatusResult", org.apache.thrift.TFieldRequirementType.REQUIRED,
                new org.apache.thrift.meta_data.EnumMetaData(org.apache.thrift.protocol.TType.ENUM, ResResult.class)));
        tmpMap.put(_Fields.NEXT_POS, new org.apache.thrift.meta_data.FieldMetaData("nextPos", org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
        tmpMap.put(_Fields.ERROR_MSG, new org.apache.thrift.meta_data.FieldMetaData("errorMsg", org.apache.thrift.TFieldRequirementType.OPTIONAL,
                new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
        metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
        org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(FileUploadResponse.class, metaDataMap);
    }

    public FileUploadResponse() {
    }

    public FileUploadResponse(
            ResResult uploadStatusResult) {
        this();
        this.uploadStatusResult = uploadStatusResult;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public FileUploadResponse(FileUploadResponse other) {
        __isset_bitfield = other.__isset_bitfield;
        if (other.isSetUploadStatusResult()) {
            this.uploadStatusResult = other.uploadStatusResult;
        }
        this.nextPos = other.nextPos;
        if (other.isSetErrorMsg()) {
            this.errorMsg = other.errorMsg;
        }
    }

    public FileUploadResponse deepCopy() {
        return new FileUploadResponse(this);
    }

    @Override
    public void clear() {
        this.uploadStatusResult = null;
        setNextPosIsSet(false);
        this.nextPos = 0;
        this.errorMsg = null;
    }

    /**
     * @see ResResult
     */
    @org.apache.thrift.annotation.Nullable
    public ResResult getUploadStatusResult() {
        return this.uploadStatusResult;
    }

    /**
     * @see ResResult
     */
    public FileUploadResponse setUploadStatusResult(@org.apache.thrift.annotation.Nullable ResResult uploadStatusResult) {
        this.uploadStatusResult = uploadStatusResult;
        return this;
    }

    public void unsetUploadStatusResult() {
        this.uploadStatusResult = null;
    }

    /**
     * Returns true if field uploadStatusResult is set (has been assigned a value) and false otherwise
     */
    public boolean isSetUploadStatusResult() {
        return this.uploadStatusResult != null;
    }

    public void setUploadStatusResultIsSet(boolean value) {
        if (!value) {
            this.uploadStatusResult = null;
        }
    }

    public long getNextPos() {
        return this.nextPos;
    }

    public FileUploadResponse setNextPos(long nextPos) {
        this.nextPos = nextPos;
        setNextPosIsSet(true);
        return this;
    }

    public void unsetNextPos() {
        __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __NEXTPOS_ISSET_ID);
    }

    /**
     * Returns true if field nextPos is set (has been assigned a value) and false otherwise
     */
    public boolean isSetNextPos() {
        return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __NEXTPOS_ISSET_ID);
    }

    public void setNextPosIsSet(boolean value) {
        __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __NEXTPOS_ISSET_ID, value);
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.String getErrorMsg() {
        return this.errorMsg;
    }

    public FileUploadResponse setErrorMsg(@org.apache.thrift.annotation.Nullable java.lang.String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public void unsetErrorMsg() {
        this.errorMsg = null;
    }

    /**
     * Returns true if field errorMsg is set (has been assigned a value) and false otherwise
     */
    public boolean isSetErrorMsg() {
        return this.errorMsg != null;
    }

    public void setErrorMsgIsSet(boolean value) {
        if (!value) {
            this.errorMsg = null;
        }
    }

    public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
        switch (field) {
            case UPLOAD_STATUS_RESULT:
                if (value == null) {
                    unsetUploadStatusResult();
                } else {
                    setUploadStatusResult((ResResult) value);
                }
                break;

            case NEXT_POS:
                if (value == null) {
                    unsetNextPos();
                } else {
                    setNextPos((java.lang.Long) value);
                }
                break;

            case ERROR_MSG:
                if (value == null) {
                    unsetErrorMsg();
                } else {
                    setErrorMsg((java.lang.String) value);
                }
                break;

        }
    }

    @org.apache.thrift.annotation.Nullable
    public java.lang.Object getFieldValue(_Fields field) {
        switch (field) {
            case UPLOAD_STATUS_RESULT:
                return getUploadStatusResult();

            case NEXT_POS:
                return getNextPos();

            case ERROR_MSG:
                return getErrorMsg();

        }
        throw new java.lang.IllegalStateException();
    }

    /**
     * Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise
     */
    public boolean isSet(_Fields field) {
        if (field == null) {
            throw new java.lang.IllegalArgumentException();
        }

        switch (field) {
            case UPLOAD_STATUS_RESULT:
                return isSetUploadStatusResult();
            case NEXT_POS:
                return isSetNextPos();
            case ERROR_MSG:
                return isSetErrorMsg();
        }
        throw new java.lang.IllegalStateException();
    }

    @Override
    public boolean equals(java.lang.Object that) {
        if (that instanceof FileUploadResponse)
            return this.equals((FileUploadResponse) that);
        return false;
    }

    public boolean equals(FileUploadResponse that) {
        if (that == null)
            return false;
        if (this == that)
            return true;

        boolean this_present_uploadStatusResult = true && this.isSetUploadStatusResult();
        boolean that_present_uploadStatusResult = true && that.isSetUploadStatusResult();
        if (this_present_uploadStatusResult || that_present_uploadStatusResult) {
            if (!(this_present_uploadStatusResult && that_present_uploadStatusResult))
                return false;
            if (!this.uploadStatusResult.equals(that.uploadStatusResult))
                return false;
        }

        boolean this_present_nextPos = true && this.isSetNextPos();
        boolean that_present_nextPos = true && that.isSetNextPos();
        if (this_present_nextPos || that_present_nextPos) {
            if (!(this_present_nextPos && that_present_nextPos))
                return false;
            if (this.nextPos != that.nextPos)
                return false;
        }

        boolean this_present_errorMsg = true && this.isSetErrorMsg();
        boolean that_present_errorMsg = true && that.isSetErrorMsg();
        if (this_present_errorMsg || that_present_errorMsg) {
            if (!(this_present_errorMsg && that_present_errorMsg))
                return false;
            if (!this.errorMsg.equals(that.errorMsg))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 8191 + ((isSetUploadStatusResult()) ? 131071 : 524287);
        if (isSetUploadStatusResult())
            hashCode = hashCode * 8191 + uploadStatusResult.getValue();

        hashCode = hashCode * 8191 + ((isSetNextPos()) ? 131071 : 524287);
        if (isSetNextPos())
            hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(nextPos);

        hashCode = hashCode * 8191 + ((isSetErrorMsg()) ? 131071 : 524287);
        if (isSetErrorMsg())
            hashCode = hashCode * 8191 + errorMsg.hashCode();

        return hashCode;
    }

    @Override
    public int compareTo(FileUploadResponse other) {
        if (!getClass().equals(other.getClass())) {
            return getClass().getName().compareTo(other.getClass().getName());
        }

        int lastComparison = 0;

        lastComparison = java.lang.Boolean.compare(isSetUploadStatusResult(), other.isSetUploadStatusResult());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetUploadStatusResult()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.uploadStatusResult, other.uploadStatusResult);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.compare(isSetNextPos(), other.isSetNextPos());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetNextPos()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.nextPos, other.nextPos);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        lastComparison = java.lang.Boolean.compare(isSetErrorMsg(), other.isSetErrorMsg());
        if (lastComparison != 0) {
            return lastComparison;
        }
        if (isSetErrorMsg()) {
            lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.errorMsg, other.errorMsg);
            if (lastComparison != 0) {
                return lastComparison;
            }
        }
        return 0;
    }

    @org.apache.thrift.annotation.Nullable
    public _Fields fieldForId(int fieldId) {
        return _Fields.findByThriftId(fieldId);
    }

    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
        scheme(iprot).read(iprot, this);
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
        scheme(oprot).write(oprot, this);
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder sb = new java.lang.StringBuilder("FileUploadResponse(");
        boolean first = true;

        sb.append("uploadStatusResult:");
        if (this.uploadStatusResult == null) {
            sb.append("null");
        } else {
            sb.append(this.uploadStatusResult);
        }
        first = false;
        if (isSetNextPos()) {
            if (!first) sb.append(", ");
            sb.append("nextPos:");
            sb.append(this.nextPos);
            first = false;
        }
        if (isSetErrorMsg()) {
            if (!first) sb.append(", ");
            sb.append("errorMsg:");
            if (this.errorMsg == null) {
                sb.append("null");
            } else {
                sb.append(this.errorMsg);
            }
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public void validate() throws org.apache.thrift.TException {
        // check for required fields
        if (uploadStatusResult == null) {
            throw new org.apache.thrift.protocol.TProtocolException("Required field 'uploadStatusResult' was not present! Struct: " + toString());
        }
        // check for sub-struct validity
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        try {
            write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
        try {
            // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
            __isset_bitfield = 0;
            read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
        } catch (org.apache.thrift.TException te) {
            throw new java.io.IOException(te);
        }
    }

    private static class FileUploadResponseStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
        public FileUploadResponseStandardScheme getScheme() {
            return new FileUploadResponseStandardScheme();
        }
    }

    private static class FileUploadResponseStandardScheme extends org.apache.thrift.scheme.StandardScheme<FileUploadResponse> {

        public void read(org.apache.thrift.protocol.TProtocol iprot, FileUploadResponse struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TField schemeField;
            iprot.readStructBegin();
            while (true) {
                schemeField = iprot.readFieldBegin();
                if (schemeField.type == org.apache.thrift.protocol.TType.STOP) {
                    break;
                }
                switch (schemeField.id) {
                    case 1: // UPLOAD_STATUS_RESULT
                        if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
                            struct.uploadStatusResult = rpc.thrift.file.transfer.ResResult.findByValue(iprot.readI32());
                            struct.setUploadStatusResultIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 2: // NEXT_POS
                        if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
                            struct.nextPos = iprot.readI64();
                            struct.setNextPosIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    case 3: // ERROR_MSG
                        if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
                            struct.errorMsg = iprot.readString();
                            struct.setErrorMsgIsSet(true);
                        } else {
                            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                        }
                        break;
                    default:
                        org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
                }
                iprot.readFieldEnd();
            }
            iprot.readStructEnd();

            // check for required fields of primitive type, which can't be checked in the validate method
            struct.validate();
        }

        public void write(org.apache.thrift.protocol.TProtocol oprot, FileUploadResponse struct) throws org.apache.thrift.TException {
            struct.validate();

            oprot.writeStructBegin(STRUCT_DESC);
            if (struct.uploadStatusResult != null) {
                oprot.writeFieldBegin(UPLOAD_STATUS_RESULT_FIELD_DESC);
                oprot.writeI32(struct.uploadStatusResult.getValue());
                oprot.writeFieldEnd();
            }
            if (struct.isSetNextPos()) {
                oprot.writeFieldBegin(NEXT_POS_FIELD_DESC);
                oprot.writeI64(struct.nextPos);
                oprot.writeFieldEnd();
            }
            if (struct.errorMsg != null) {
                if (struct.isSetErrorMsg()) {
                    oprot.writeFieldBegin(ERROR_MSG_FIELD_DESC);
                    oprot.writeString(struct.errorMsg);
                    oprot.writeFieldEnd();
                }
            }
            oprot.writeFieldStop();
            oprot.writeStructEnd();
        }

    }

    private static class FileUploadResponseTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
        public FileUploadResponseTupleScheme getScheme() {
            return new FileUploadResponseTupleScheme();
        }
    }

    private static class FileUploadResponseTupleScheme extends org.apache.thrift.scheme.TupleScheme<FileUploadResponse> {

        @Override
        public void write(org.apache.thrift.protocol.TProtocol prot, FileUploadResponse struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            oprot.writeI32(struct.uploadStatusResult.getValue());
            java.util.BitSet optionals = new java.util.BitSet();
            if (struct.isSetNextPos()) {
                optionals.set(0);
            }
            if (struct.isSetErrorMsg()) {
                optionals.set(1);
            }
            oprot.writeBitSet(optionals, 2);
            if (struct.isSetNextPos()) {
                oprot.writeI64(struct.nextPos);
            }
            if (struct.isSetErrorMsg()) {
                oprot.writeString(struct.errorMsg);
            }
        }

        @Override
        public void read(org.apache.thrift.protocol.TProtocol prot, FileUploadResponse struct) throws org.apache.thrift.TException {
            org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
            struct.uploadStatusResult = rpc.thrift.file.transfer.ResResult.findByValue(iprot.readI32());
            struct.setUploadStatusResultIsSet(true);
            java.util.BitSet incoming = iprot.readBitSet(2);
            if (incoming.get(0)) {
                struct.nextPos = iprot.readI64();
                struct.setNextPosIsSet(true);
            }
            if (incoming.get(1)) {
                struct.errorMsg = iprot.readString();
                struct.setErrorMsgIsSet(true);
            }
        }
    }

    private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
        return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
    }
}

