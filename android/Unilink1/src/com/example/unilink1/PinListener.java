package com.example.unilink1;
import com.google.android.gms.maps.model.Marker;

public interface PinListener {
	Marker OnNewPin(BasePin p);
	Marker OnUpdatePin(BasePin p);
}
