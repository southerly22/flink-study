package Test;

public interface StreamTaskInput extends ParentStreamInput {

    int getInputIndex();
    String prepareSnapshot();
}
