package checks;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.function.Executable;

public class UnreachableCatchCheck {
  void unreachable(boolean cond) {
    try {
      throwCustomDerivedException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Noncompliant [[sc=14;ec=29;secondary=14]] {{Remove this type because it is unreachable as hidden by previous catch blocks.}}
      // ...
    }

    try {
      throw new CustomDerivedException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException|IllegalStateException e) { // Noncompliant [[sc=14;ec=29]]
      // ...
    }

    try {
      new ThrowingCustomDerivedException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Noncompliant
      // ...
    }

    try {
      throwIOException();
    } catch (IOException e) {
      // ...
    } catch (Exception e) { // Compliant, also catch runtime exception
      // ...
    }

    try {
      throwCustomDerivedDerivedException();
    } catch (CustomDerivedDerivedException e) {
      // ...
    } catch (CustomDerivedException e) { // Noncompliant [[secondary=46]]
      // ...
    } catch (CustomException e) { // Noncompliant [[secondary=46,48]]
      // ...
    }

    try {
      throwCustomDerivedException();
    } catch (CustomDerivedDerivedException e) {
      // ...
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Noncompliant [[secondary=58]]
      // ...
    }

    try {
      throwCustomDerivedException();
      throwIOException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (IOException e) { // Compliant
      // ...
    } catch (CustomException e) { // FN, line 67 hide this catch, but we don't support correctly the presence of another type of Exception
      // ...
    }

    try {
      throwCustomDerivedDerivedException();
      throwIOException();
    } catch (CustomDerivedException | IOException e) {
      // ...
    } catch (CustomException e) { // FN, we don't support correctly the presence of another type of Exception
      // ...
    }

    try {
      throwCustomDerivedException();

      class Inner {
        void f() throws CustomException {
          throwCustomException(); // not in the same scope
        }
      }
      takeObject((Executable)(() -> throwCustomException())); // not in the same scope
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Noncompliant
      // ...
    }

    try {
      throwCustomException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Compliant, a CustomException is thrown, this is reachable
      // ...
    }

    try {
      throw new CustomException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // compliant
      // ...
    }

    try {
      throwCustomException();
    } catch (CustomDerivedDerivedException e) {
      // ...
    } catch (CustomDerivedException e) { // Compliant, throwCustomException can throw one of his subtype
      // ...
    } catch (CustomException e) {
      // ...
    }

    try {
      throw new Exception();
    } catch (CustomDerivedDerivedException e) {
      // ...
    } catch (CustomDerivedException e) { // Compliant, FN in this case, but Exception could be one of his subtype
      // ...
    } catch (Exception e) {
      // ...
    }

    try {
      throwCustomDerivedDerivedException();
      throw new Exception();
    } catch (CustomDerivedDerivedException e) {
      // ...
    } catch (CustomDerivedException e) { // Compliant
      // ...
    } catch (Exception e) {
      // ...
    }

    try {
      throwCustomDerivedException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (Throwable e) { // Compliant
      // ...
    }

    try {
      if (cond) {
        throwCustomException();
      } else {
        throwCustomDerivedException();
      }
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Compliant, a CustomException is thrown, this is reachable
      // ...
    }

    try {
      if (cond) {
        throwCustomDerivedException();
      } else {
        throwCustomException();
      }
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Compliant
      // ...
    }

    try {
      throwCustomException();
    } catch (CustomException e) { // Compliant
      // ...
    }

    try(InputStream input = new FileInputStream("reportFileName")) {
      throwCustomDerivedException();
    } catch (FileNotFoundException e) {
    } catch (IOException e) { // Compliant, close throws an IOException
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // FN
      // ...
    }

    class B implements Closeable {
      @Override
      public void close() throws FileNotFoundException {
        throw new RuntimeException();
      }
    }

    try (B a = new B()) {
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) { // FN: this can happen only if the class implementing Closable specify a subtype of IOException in the declaration.
      e.printStackTrace();
    }

    try {
      throwIllegalArgumentException();
    } catch (IllegalArgumentException e) {
      // ...
    } catch (Error e) {
      // ...
    } catch (RuntimeException e) { // Compliant, not a checked exception
      // ...
    }

    try {
      throwCustomDerivedException();
      throwOtherCustomDerivedException();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Compliant
      // ...
    }

    try {
      throwBoth();
    } catch (CustomDerivedException e) {
      // ...
    } catch (CustomException e) { // Compliant
      // ...
    }

  }

  void throwCustomException() throws CustomException {
    throw new CustomException();
  }

  void throwCustomDerivedException() throws CustomDerivedException {
    throw new CustomDerivedException();
  }

  void throwBoth() throws CustomDerivedException,OtherCustomDerivedException {
  }

  void throwOtherCustomDerivedException() throws OtherCustomDerivedException {
    throw new OtherCustomDerivedException();
  }

  void throwCustomDerivedDerivedException() throws CustomDerivedDerivedException {
    throw new CustomDerivedDerivedException();
  }

  void throwIOException() throws IOException {
    throw new IOException();
  }

  void throwIllegalArgumentException() throws IllegalArgumentException {
    throw new IllegalArgumentException();
  }

  void takeObject(Object o) {

  }

  public static class CustomException extends Exception {
  }

  public static class CustomDerivedException extends CustomException {
  }

  public static class OtherCustomDerivedException extends CustomException {
  }

  public static class CustomDerivedDerivedException extends CustomDerivedException {
  }

  class ThrowingCustomDerivedException {
    ThrowingCustomDerivedException() throws CustomDerivedException {

    }
  }

}