package hr.vsite.dipl.quicktimetable.notifications;

import android.app.Notification;
import android.graphics.drawable.Icon;
import android.os.Parcel;

/**
 * Created by Jure on 15.7.2016..
 */
public class QttNotification extends Notification {

    static Creator CREATOR;//?????????????????

    public QttNotification(Parcel parcel) {
        super(parcel);
    }

    @Override
    public String getGroup() {
        return super.getGroup();
    }

    @Override
    public String getSortKey() {
        return super.getSortKey();
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
    }

    @Override
    public Icon getSmallIcon() {
        return super.getSmallIcon();
    }

    @Override
    public Icon getLargeIcon() {
        return super.getLargeIcon();
    }
}
