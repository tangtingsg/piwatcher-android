package sg.nus.tangting.PiWatcher;

import android.os.Parcel;
import android.os.Parcelable;

public class Movement implements Parcelable {
    private String mMessage;
    private long mTimestamp;


    public Movement() {
        mMessage = "";
        mTimestamp = 0;
    }

    public Movement(String message, long timestamp) {
        this.mMessage = message;
        this.mTimestamp = timestamp;
    }

    public String getMessage() {
        return mMessage;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public static final Creator<Movement> CREATOR = new Creator<Movement>(){
        @Override
        public Movement createFromParcel(Parcel source) {
            Movement movement = new Movement();
            movement.mMessage = source.readString();
            movement.mTimestamp = source.readLong();
            return movement;
        }

        @Override
        public Movement[] newArray(int size) {
            return new Movement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeLong(mTimestamp);
    }
}
