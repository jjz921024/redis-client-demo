package io.jjz.common;

import io.lettuce.core.protocol.ProtocolKeyword;

import java.nio.charset.StandardCharsets;

public enum ExtendCommandType implements ProtocolKeyword {

    CAS, CAD, EXSET, EXGET, EXSETVER, EXINCRBY, EXINCRBYFLOAT,
    EXCAS, EXCAD, EXAPPEND, EXPREPEND, EXGAE,

    EXHSET, EXHGET, EXHMSET, EXHPEXPIREAT, EXHPEXPIRE, EXHEXPIREAT, EXHEXPIRE,
    EXHPTTL, EXHTTL, EXHVER, EXHSETVER, EXHINCRBY, EXHINCRBYFLOAT,
    EXHGETWITHVER, EXHMGET, EXHMGETWITHVER, EXHDEL, EXHLEN, EXHEXISTS, EXHSTRLEN,
    EXHKEYS, EXHVALS, EXHGETALL, EXHSCAN;

    public final byte[] bytes;

    ExtendCommandType() {
        bytes = name().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

}