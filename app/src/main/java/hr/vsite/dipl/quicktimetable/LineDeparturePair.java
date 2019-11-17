package hr.vsite.dipl.quicktimetable;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Jure on 3.6.2017..
 */

public class LineDeparturePair implements Parcelable {
    private String lineNumber;
    private String departureTime;

    public LineDeparturePair(String lineNumber, String departureTime) {
        this.lineNumber = lineNumber;
        this.departureTime = departureTime;
    }

    protected LineDeparturePair(Parcel in) {
        lineNumber = in.readString();
        departureTime = in.readString();
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(lineNumber);
        dest.writeString(departureTime);
    }

    public static final Creator<LineDeparturePair> CREATOR = new Creator<LineDeparturePair>() {
        @Override
        public LineDeparturePair createFromParcel(Parcel in) {
            return new LineDeparturePair(in);
        }

        @Override
        public LineDeparturePair[] newArray(int size) {
            return new LineDeparturePair[size];
        }
    };
}
