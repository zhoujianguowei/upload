/**
 * Autogenerated by Thrift Compiler (0.15.0)
 * <p>
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *
 * @generated
 */
package rpc.thrift.file.transfer;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.15.0)", date = "2021-10-12")
public enum EncryptTypeEnum implements org.apache.thrift.TEnum {
    MD5_TYPE(1),
    BASE64_TYPE(2),
    BYTE_REVERSE(3);

    private final int value;

    private EncryptTypeEnum(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this enum value, as defined in the Thrift IDL.
     */
    public int getValue() {
        return value;
    }

    /**
     * Find a the enum type by its integer value, as defined in the Thrift IDL.
     * @return null if the value is not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static EncryptTypeEnum findByValue(int value) {
        switch (value) {
            case 1:
                return MD5_TYPE;
            case 2:
                return BASE64_TYPE;
            case 3:
                return BYTE_REVERSE;
            default:
                return null;
        }
    }
}
