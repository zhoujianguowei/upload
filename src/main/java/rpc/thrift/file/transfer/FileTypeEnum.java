/**
 * Autogenerated by Thrift Compiler (0.14.1)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package rpc.thrift.file.transfer;


@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.14.1)", date = "2021-05-11")
public enum FileTypeEnum implements org.apache.thrift.TEnum {
  FILE_TYPE(0),
  DIR_TYPE(1);

  private final int value;

  private FileTypeEnum(int value) {
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
  public static FileTypeEnum findByValue(int value) { 
    switch (value) {
      case 0:
        return FILE_TYPE;
      case 1:
        return DIR_TYPE;
      default:
        return null;
    }
  }
}
