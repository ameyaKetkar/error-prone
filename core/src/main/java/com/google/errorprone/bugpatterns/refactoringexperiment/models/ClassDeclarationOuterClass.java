// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ClassDeclaration.proto

package com.google.errorprone.bugpatterns.refactoringexperiment.models;

public final class ClassDeclarationOuterClass {
  private ClassDeclarationOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface ClassDeclarationOrBuilder extends
      // @@protoc_insertion_point(interface_extends:Models.ClassDeclaration)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    boolean hasId();
    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification getId();
    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder getIdOrBuilder();

    /**
     * <code>repeated string super_type = 2;</code>
     */
    java.util.List<java.lang.String>
        getSuperTypeList();
    /**
     * <code>repeated string super_type = 2;</code>
     */
    int getSuperTypeCount();
    /**
     * <code>repeated string super_type = 2;</code>
     */
    java.lang.String getSuperType(int index);
    /**
     * <code>repeated string super_type = 2;</code>
     */
    com.google.protobuf.ByteString
        getSuperTypeBytes(int index);
  }
  /**
   * Protobuf type {@code Models.ClassDeclaration}
   */
  public  static final class ClassDeclaration extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:Models.ClassDeclaration)
      ClassDeclarationOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use ClassDeclaration.newBuilder() to construct.
    private ClassDeclaration(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private ClassDeclaration() {
      superType_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private ClassDeclaration(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder subBuilder = null;
              if (((bitField0_ & 0x00000001) == 0x00000001)) {
                subBuilder = id_.toBuilder();
              }
              id_ = input.readMessage(com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.PARSER, extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(id_);
                id_ = subBuilder.buildPartial();
              }
              bitField0_ |= 0x00000001;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
                superType_ = new com.google.protobuf.LazyStringArrayList();
                mutable_bitField0_ |= 0x00000002;
              }
              superType_.add(bs);
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
          superType_ = superType_.getUnmodifiableView();
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.internal_static_Models_ClassDeclaration_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.internal_static_Models_ClassDeclaration_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.class, com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.Builder.class);
    }

    private int bitField0_;
    public static final int ID_FIELD_NUMBER = 1;
    private com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification id_;
    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    public com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification getId() {
      return id_ == null ? com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.getDefaultInstance() : id_;
    }
    /**
     * <code>optional .Models.Identification id = 1;</code>
     */
    public com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder getIdOrBuilder() {
      return id_ == null ? com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.getDefaultInstance() : id_;
    }

    public static final int SUPER_TYPE_FIELD_NUMBER = 2;
    private com.google.protobuf.LazyStringList superType_;
    /**
     * <code>repeated string super_type = 2;</code>
     */
    public com.google.protobuf.ProtocolStringList
        getSuperTypeList() {
      return superType_;
    }
    /**
     * <code>repeated string super_type = 2;</code>
     */
    public int getSuperTypeCount() {
      return superType_.size();
    }
    /**
     * <code>repeated string super_type = 2;</code>
     */
    public java.lang.String getSuperType(int index) {
      return superType_.get(index);
    }
    /**
     * <code>repeated string super_type = 2;</code>
     */
    public com.google.protobuf.ByteString
        getSuperTypeBytes(int index) {
      return superType_.getByteString(index);
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeMessage(1, getId());
      }
      for (int i = 0; i < superType_.size(); i++) {
        com.google.protobuf.GeneratedMessageV3.writeString(output, 2, superType_.getRaw(i));
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, getId());
      }
      {
        int dataSize = 0;
        for (int i = 0; i < superType_.size(); i++) {
          dataSize += computeStringSizeNoTag(superType_.getRaw(i));
        }
        size += dataSize;
        size += 1 * getSuperTypeList().size();
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration)) {
        return super.equals(obj);
      }
      com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration other = (com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration) obj;

      boolean result = true;
      result = result && (hasId() == other.hasId());
      if (hasId()) {
        result = result && getId()
            .equals(other.getId());
      }
      result = result && getSuperTypeList()
          .equals(other.getSuperTypeList());
      result = result && unknownFields.equals(other.unknownFields);
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (hasId()) {
        hash = (37 * hash) + ID_FIELD_NUMBER;
        hash = (53 * hash) + getId().hashCode();
      }
      if (getSuperTypeCount() > 0) {
        hash = (37 * hash) + SUPER_TYPE_FIELD_NUMBER;
        hash = (53 * hash) + getSuperTypeList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code Models.ClassDeclaration}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:Models.ClassDeclaration)
        com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclarationOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.internal_static_Models_ClassDeclaration_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.internal_static_Models_ClassDeclaration_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.class, com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.Builder.class);
      }

      // Construct using com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
          getIdFieldBuilder();
        }
      }
      public Builder clear() {
        super.clear();
        if (idBuilder_ == null) {
          id_ = null;
        } else {
          idBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        superType_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.internal_static_Models_ClassDeclaration_descriptor;
      }

      public com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration getDefaultInstanceForType() {
        return com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.getDefaultInstance();
      }

      public com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration build() {
        com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration buildPartial() {
        com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration result = new com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        if (idBuilder_ == null) {
          result.id_ = id_;
        } else {
          result.id_ = idBuilder_.build();
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          superType_ = superType_.getUnmodifiableView();
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.superType_ = superType_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration) {
          return mergeFrom((com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration other) {
        if (other == com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration.getDefaultInstance()) return this;
        if (other.hasId()) {
          mergeId(other.getId());
        }
        if (!other.superType_.isEmpty()) {
          if (superType_.isEmpty()) {
            superType_ = other.superType_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureSuperTypeIsMutable();
            superType_.addAll(other.superType_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification id_ = null;
      private com.google.protobuf.SingleFieldBuilderV3<
          com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder> idBuilder_;
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public boolean hasId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification getId() {
        if (idBuilder_ == null) {
          return id_ == null ? com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.getDefaultInstance() : id_;
        } else {
          return idBuilder_.getMessage();
        }
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public Builder setId(com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification value) {
        if (idBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          id_ = value;
          onChanged();
        } else {
          idBuilder_.setMessage(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public Builder setId(
          com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder builderForValue) {
        if (idBuilder_ == null) {
          id_ = builderForValue.build();
          onChanged();
        } else {
          idBuilder_.setMessage(builderForValue.build());
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public Builder mergeId(com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification value) {
        if (idBuilder_ == null) {
          if (((bitField0_ & 0x00000001) == 0x00000001) &&
              id_ != null &&
              id_ != com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.getDefaultInstance()) {
            id_ =
              com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.newBuilder(id_).mergeFrom(value).buildPartial();
          } else {
            id_ = value;
          }
          onChanged();
        } else {
          idBuilder_.mergeFrom(value);
        }
        bitField0_ |= 0x00000001;
        return this;
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public Builder clearId() {
        if (idBuilder_ == null) {
          id_ = null;
          onChanged();
        } else {
          idBuilder_.clear();
        }
        bitField0_ = (bitField0_ & ~0x00000001);
        return this;
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder getIdBuilder() {
        bitField0_ |= 0x00000001;
        onChanged();
        return getIdFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      public com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder getIdOrBuilder() {
        if (idBuilder_ != null) {
          return idBuilder_.getMessageOrBuilder();
        } else {
          return id_ == null ?
              com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.getDefaultInstance() : id_;
        }
      }
      /**
       * <code>optional .Models.Identification id = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder> 
          getIdFieldBuilder() {
        if (idBuilder_ == null) {
          idBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.Identification.Builder, com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.IdentificationOrBuilder>(
                  getId(),
                  getParentForChildren(),
                  isClean());
          id_ = null;
        }
        return idBuilder_;
      }

      private com.google.protobuf.LazyStringList superType_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      private void ensureSuperTypeIsMutable() {
        if (!((bitField0_ & 0x00000002) == 0x00000002)) {
          superType_ = new com.google.protobuf.LazyStringArrayList(superType_);
          bitField0_ |= 0x00000002;
         }
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public com.google.protobuf.ProtocolStringList
          getSuperTypeList() {
        return superType_.getUnmodifiableView();
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public int getSuperTypeCount() {
        return superType_.size();
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public java.lang.String getSuperType(int index) {
        return superType_.get(index);
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public com.google.protobuf.ByteString
          getSuperTypeBytes(int index) {
        return superType_.getByteString(index);
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public Builder setSuperType(
          int index, java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureSuperTypeIsMutable();
        superType_.set(index, value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public Builder addSuperType(
          java.lang.String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureSuperTypeIsMutable();
        superType_.add(value);
        onChanged();
        return this;
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public Builder addAllSuperType(
          java.lang.Iterable<java.lang.String> values) {
        ensureSuperTypeIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, superType_);
        onChanged();
        return this;
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public Builder clearSuperType() {
        superType_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }
      /**
       * <code>repeated string super_type = 2;</code>
       */
      public Builder addSuperTypeBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureSuperTypeIsMutable();
        superType_.add(value);
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:Models.ClassDeclaration)
    }

    // @@protoc_insertion_point(class_scope:Models.ClassDeclaration)
    private static final com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration();
    }

    public static com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @java.lang.Deprecated public static final com.google.protobuf.Parser<ClassDeclaration>
        PARSER = new com.google.protobuf.AbstractParser<ClassDeclaration>() {
      public ClassDeclaration parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new ClassDeclaration(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<ClassDeclaration> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<ClassDeclaration> getParserForType() {
      return PARSER;
    }

    public com.google.errorprone.bugpatterns.refactoringexperiment.models.ClassDeclarationOuterClass.ClassDeclaration getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Models_ClassDeclaration_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Models_ClassDeclaration_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\026ClassDeclaration.proto\022\006Models\032\024Identi" +
      "fication.proto\"J\n\020ClassDeclaration\022\"\n\002id" +
      "\030\001 \001(\0132\026.Models.Identification\022\022\n\nsuper_" +
      "type\030\002 \003(\tB@\n>com.google.errorprone.bugp" +
      "atterns.refactoringexperiment.models"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.getDescriptor(),
        }, assigner);
    internal_static_Models_ClassDeclaration_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_Models_ClassDeclaration_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Models_ClassDeclaration_descriptor,
        new java.lang.String[] { "Id", "SuperType", });
    com.google.errorprone.bugpatterns.refactoringexperiment.models.IdentificationOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}