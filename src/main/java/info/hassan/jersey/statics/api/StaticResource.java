package info.hassan.jersey.statics.api;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.nullsFirst;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public class StaticResource implements Comparable<StaticResource> {

  private final int statusCode;
  private String mimeType;
  private byte[] data;

  public StaticResource(int statusCode) {
    this.statusCode = statusCode;
  }

  public StaticResource(int statusCode, String mimeType, byte[] data) {
    this.statusCode = statusCode;
    this.mimeType = mimeType;
    this.data = data;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public boolean isResponseOk() {
    return this.statusCode == 200;
  }

  public boolean isResponseNotFound() {
    return this.statusCode == 404;
  }

  public boolean isResponseServerError() {
    return this.statusCode >= 500;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean hasMimeType() {
    return null != this.mimeType && !this.mimeType.isEmpty();
  }

  public boolean hasData() {
    return this.data != null && this.data.length > 0;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", StaticResource.class.getSimpleName() + "[", "]")
        .add("statusCode=" + statusCode)
        .add("mimeType='" + mimeType + "'")
        .add("dataLength=" + data.length)
        .toString();
  }

  @Override
  public boolean equals(Object that) {
    if (this == that) return true;
    if (!(that instanceof StaticResource)) return false;
    StaticResource thatOne = (StaticResource) that;
    return statusCode == thatOne.statusCode
        && mimeType.equals(thatOne.mimeType)
        && Arrays.equals(data, thatOne.data);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(statusCode, mimeType);
    result = 31 * result + Arrays.hashCode(data);
    return result;
  }

  @Override
  public int compareTo(StaticResource that) {
    return nullsFirst(
            comparingInt(StaticResource::getStatusCode)
                .thenComparing(StaticResource::getMimeType)
                .thenComparing(
                    (one, two) -> {
                      if (two.getData() == null) {
                        return 1;
                      } else if (one.getData() == null) {
                        return -1;
                      }
                      return Integer.compare(one.getData().length, two.getData().length);
                    }))
        .compare(this, that);
  }
}
