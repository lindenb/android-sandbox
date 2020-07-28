package com.github.lindenb.android.apps.drawinggrid;

import android.os.Parcel;
import android.os.Parcelable;

// https://stackoverflow.com/questions/2139134/how-to-send-an-object-from-one-android-activity-to-another-using-intents
public class GridParcelable  implements Parcelable {
    private String imagepath;
    private boolean rotate = false;
    private boolean square = false;
    private int gridSize = 10;
    public GridParcelable() {

    }
    private GridParcelable(Parcel in) {
        this.imagepath = in.readString();
        this.rotate = in.readInt()==1;
        this.square = in.readInt()==1;
        this.gridSize = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.imagepath==null?"":this.imagepath);
        out.writeInt(this.rotate?1:0);
        out.writeInt(this.square?1:0);
        out.writeInt(this.gridSize);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<GridParcelable> CREATOR = new Parcelable.Creator<GridParcelable>() {
        public GridParcelable createFromParcel(Parcel in) {
            return new GridParcelable(in);
        }

        public GridParcelable[] newArray(int size) {
            return new GridParcelable[size];
        }
    };
}
