package com.example.gcorddemo.viewmodel;

import android.content.res.AssetManager;

import androidx.lifecycle.ViewModel;

import com.example.gcorddemo.application.DemoApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public abstract class BaseViewModel extends ViewModel {

    String getStringFromAssetsFile(String fileName) throws IOException {
        AssetManager assetManager = DemoApplication.getApplication().getAssets();
        if (assetManager != null) {
            InputStream inputStream = assetManager.open(fileName);
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            while (inputStream.read(buffer) > 0) {
                byteArrayOutputStream.write(buffer);
            }
            byteArrayOutputStream.flush();
            String string = new String(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            return string;
        }
        return "";
    }

    public static class DemoItem implements Serializable {
        static final long serialVersionUID = 42L;

        private String name;
        private String description;
        private int id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        @SuppressWarnings("unused")
        public void setDescription(String description) {
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

}
