package au.edu.curtin.interactiveapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class MainActivity extends AppCompatActivity {

    /* Constants */
    private static final int REQUEST_THUMBNAIL = 1;
    private static final int REQUEST_CONTACT = 2;

    /* Fields */
    private EditText phoneNumberText;
    private EditText latitudeText;
    private EditText longitudeText;

    private ImageView thumbnailImage;

    private TextView contactIDText;
    private TextView contactNameText;
    private TextView contactEmailText;
    private TextView contactPhoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get UI Elements
        phoneNumberText = findViewById(R.id.txtPhoneNumber);
        latitudeText = findViewById(R.id.txtLatitude);
        longitudeText = findViewById(R.id.txtLongitude);

        thumbnailImage = findViewById(R.id.imgThumbnail);

        contactIDText = findViewById(R.id.txtContactID);
        contactNameText = findViewById(R.id.txtContactName);
        contactEmailText = findViewById(R.id.txtContactEmail);
        contactPhoneText = findViewById(R.id.txtContactPhone);

        Button callButton = findViewById(R.id.btnCall);
        Button mapButton = findViewById(R.id.btnShowLocation);
        Button photoButton = findViewById(R.id.btnPhoto);
        Button contactButton = findViewById(R.id.btnSearchContacts);

        // Set On click listeners
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String callString = "tel:" + phoneNumberText.getText().toString();
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(callString));
                startActivity(callIntent);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mapString = "geo:";
                mapString += latitudeText.getText().toString() + ",";
                mapString += longitudeText.getText().toString();
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapString));
                startActivity(mapIntent);
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent thumbnailIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(thumbnailIntent, REQUEST_THUMBNAIL);
            }
        });

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT);
                startActivityForResult(contactIntent, REQUEST_CONTACT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            switch (requestCode) {
                case REQUEST_THUMBNAIL:
                    setThumbnailImage(intent);
                    break;

                case REQUEST_CONTACT:
                    setContactInfo(intent);
                    break;
            }
        }
    }

    private void setThumbnailImage(Intent intent) {
        Bitmap thumbnail = (Bitmap) intent.getExtras().get("data");
        thumbnailImage.setImageBitmap(thumbnail);
    }

    private void setContactInfo(Intent intent) {
        // Variables
        int contactID = 0;
        String contactName = "";
        String contactEmail = "";
        String contactPhone = "";

        Uri contactUri = intent.getData();

        String[] queryFields = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        Cursor cursor = this.getContentResolver().query(contactUri, queryFields, null, null, null);

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                contactID = cursor.getInt(0);
                contactName = cursor.getString(1);

                // Get email address
                Cursor emailCursor = this.getContentResolver().query(
                        Email.CONTENT_URI,
                        new String[] {Email.ADDRESS},
                        Email.CONTACT_ID + " = ?",
                        new String[] {String.valueOf(contactID)},
                        null, null
                );

                try {
                    if (emailCursor.getCount() > 0) {
                        emailCursor.moveToFirst();
                        contactEmail = emailCursor.getString(0);
                    }
                }
                finally {
                    emailCursor.close();
                }

                // Get phone number
                Cursor phoneCursor = this.getContentResolver().query(
                        Phone.CONTENT_URI,
                        new String[] {Phone.NUMBER},
                        Phone.CONTACT_ID + " = ?",
                        new String[] {String.valueOf(contactID)},
                        null, null
                );

                try{
                    if (phoneCursor.getCount() > 0) {
                        phoneCursor.moveToFirst();
                        contactPhone = phoneCursor.getString(0);
                    }
                }
                finally {
                    phoneCursor.close();
                }

                contactIDText.setText(String.format("%d", contactID));
                contactNameText.setText(contactName);
                contactEmailText.setText(contactEmail);
                contactPhoneText.setText(contactPhone);
            }
        }
        finally {
            cursor.close();
        }
    }
}
