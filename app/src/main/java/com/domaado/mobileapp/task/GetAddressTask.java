package com.domaado.mobileapp.task;

import android.os.AsyncTask;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by jameshong on 2018. 6. 8..
 */

public class GetAddressTask extends AsyncTask<TMapPoint, String, String> {

    @Override
    protected String doInBackground(TMapPoint... tMapPoints) {
        String addr = null;

        TMapData tMapData = new TMapData();

        try {
            addr = tMapData.convertGpsToAddress(tMapPoints[0].getLatitude(), tMapPoints[0].getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return addr;
    }
}
