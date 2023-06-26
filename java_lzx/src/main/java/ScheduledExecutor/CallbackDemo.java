package ScheduledExecutor;

/**
 * @author lzx
 * @date 2023/06/25 20:58
 * todo : 采用内部类实现回调
 **/
public class CallbackDemo {
    public static void main(String[] args) {
        Button button = new Button();

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick() {
                System.out.println("button clicked");
            }
        });

        button.click();
    }

    interface OnClickListener {
        void onClick();
    }

    static class Button {
        private OnClickListener onClickListener;

        public void setOnClickListener(OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        public void click() {
            System.out.println("Clicking the button...");
            if (onClickListener != null) {
                onClickListener.onClick();
            }
        }
    }
}



