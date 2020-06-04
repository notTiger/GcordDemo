package com.example.gcorddemo.model;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.example.gcorddemo.application.DemoApplication;
import com.example.gcorddemo.bean.ContactBean;

import java.util.ArrayList;

public enum ContactModel {
    instance;

    private static String preHandlePhoneNumberPrefix(@NonNull String phoneNumber) {
        if (phoneNumber.startsWith("+86")) {
            phoneNumber = phoneNumber.replace("+86", "");
        }
        if (phoneNumber.startsWith("86")) {
            phoneNumber = phoneNumber.replace("86", "");
        }

        if (phoneNumber.startsWith("01"))
            phoneNumber = phoneNumber.substring(1);
        return phoneNumber;
    }

    public static boolean isStringNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public boolean insert(String givenName, String familyName, String phoneNumber, String company) throws OperationApplicationException, RemoteException {
        String displayName = familyName;
        if (displayName == null) displayName = "";
        if (givenName != null)
            displayName = displayName.trim() + givenName.trim();
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        ContentProviderOperation op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)  // 此处传入null添加一个raw_contact空数据
                .build();

        ops.add(op);

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

                // Sets the data row's display name to the name in the UI.
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                .build();

        ops.add(op);

        op =
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        /*
                         * Sets the value of the raw contact id column to the new raw contact ID returned
                         * by the first operation in the batch.
                         */
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                        // Sets the data row's MIME type to Phone
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

                        // Sets the phone number and type
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build();
        ops.add(op);

        op =
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        /*
                         * Sets the value of the raw contact id column to the new raw contact ID returned
                         * by the first operation in the batch.
                         */
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                        // Sets the data row's MIME type to Phone
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)

                        // Sets the company
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company).build();
        ops.add(op);
        DemoApplication.getApplication().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        return true;
    }

    public boolean update(ContactBean item) throws OperationApplicationException, RemoteException {
        String familyName = item.getFamilyName();
        String displayName = familyName;
        if (displayName == null) displayName = "";
        String givenName = item.getGivenName();
        if (givenName != null)
            displayName = displayName.trim() + givenName.trim();
        ArrayList<ContentProviderOperation> ops =
                new ArrayList<>();
        String structuredNameId = item.getStructuredNameId();
        ContentProviderOperation op;
        if (!ContactModel.isStringNullOrEmpty(structuredNameId)) {
            op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.CommonDataKinds.Identity._ID + "=?", new String[]{structuredNameId})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                    .build();
        } else {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.CommonDataKinds.Identity.RAW_CONTACT_ID, item.getRawContactId())
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.MIMETYPE)
                    .build();
        }

        ops.add(op);

        String organizationId = item.getOrganizationId();
        if (!ContactModel.isStringNullOrEmpty(organizationId)) {
            op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.CommonDataKinds.Identity._ID + "=?", new String[]{organizationId})
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, item.getCompany())
                    .build();
        } else {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.CommonDataKinds.Identity.RAW_CONTACT_ID, item.getRawContactId())
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.MIMETYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, item.getCompany())
                    .build();
        }

        ops.add(op);

        for (ContactBean.PhoneData phoneData :
                item.getPhoneDataList()) {
            String id = phoneData.getDataId();
            if (!ContactModel.isStringNullOrEmpty(id)) {
                op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.CommonDataKinds.Identity._ID + "=?", new String[]{id})
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneData.getPhoneNumber())
                        .build();
            } else {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.CommonDataKinds.Identity.RAW_CONTACT_ID, item.getRawContactId())
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.MIMETYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneData.getPhoneNumber())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build();
            }
            ops.add(op);
        }
        DemoApplication.getApplication().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        return true;
    }

    public boolean delete(@NonNull ContactBean item) {
        try {
            int result = DemoApplication.getApplication().getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                    ContactsContract.RawContacts._ID + "=?", new String[]{item.getRawContactId()});
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<ContactBean> queryAllContacts() {
        Context context = DemoApplication.getApplication();
        ArrayList<ContactBean> contactBeans = new ArrayList<>();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        try (Cursor data = context.getContentResolver().query(uri, null, null, null, null)) {
            if (data != null) {
                boolean hasNext = data.moveToFirst();
                while (hasNext) {
                    String contactId = data.getString(data.getColumnIndex(ContactsContract.Contacts._ID));
                    String displayName = data.getString(data.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    ArrayList<ContactBean> list = queryRawContacts(contactId, displayName);
                    contactBeans.addAll(list);
                    hasNext = data.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return contactBeans;
    }

    private @NonNull
    ArrayList<ContactBean> queryRawContacts(String contactId, String displayName) {
        Uri uri = ContactsContract.RawContacts.CONTENT_URI;
        Context context = DemoApplication.getApplication();
        ArrayList<ContactBean> contactBeans = new ArrayList<>();
        try (Cursor rawData = context.getContentResolver().query(
                uri,
                null,
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{contactId},
                null)) {
            if (rawData != null) {
                boolean hasNext = rawData.moveToFirst();
                while (hasNext) {
                    ContactBean contactBean = new ContactBean();
                    contactBean.setDisplayName(displayName);
                    int index = rawData.getColumnIndex(ContactsContract.RawContacts._ID);
                    if (index > 0) {
                        String rawDataId = rawData.getString(index);
                        contactBean.setRawContactId(rawDataId);
                        queryData(rawDataId, contactBean);
                        contactBeans.add(contactBean);
                    }
                    hasNext = rawData.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return contactBeans;
    }

    private void queryData(String rawDataId, ContactBean contactBean) {
        Uri uri = ContactsContract.Data.CONTENT_URI;
        Context context = DemoApplication.getApplication();
        try (Cursor data = context.getContentResolver().query(uri,
                null,
                ContactsContract.Data.RAW_CONTACT_ID + "=?",
                new String[]{rawDataId},
                null)) {
            if (data != null) {
                boolean hasNext = data.moveToNext();
                while (hasNext) {
                    String mimeType = data.getString(data.getColumnIndex(ContactsContract.Data.MIMETYPE));
                    int index;
                    if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String number = "";
                        if (index > 0) {
                            number = data.getString(index);
                        }
                        contactBean.addPhoneNumber(data.getString(data.getColumnIndex(ContactsContract.Data._ID)), number);
                    } else if (ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY);
                        String company = "";
                        if (index >= 0) {
                            company = data.getString(index);
                        }
                        contactBean.setCompany(company);
                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID);
                        contactBean.setOrganizationId(data.getString(index));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);

                        String familyName = "";
                        if (index > 0) {
                            familyName = data.getString(index);
                        }

                        String givenName = "";
                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
                        if (index > 0) {
                            givenName = data.getString(index);
                        }


                        contactBean.setFamilyName(familyName);
                        contactBean.setGivenName(givenName);

                        index = data.getColumnIndex(ContactsContract.CommonDataKinds.Identity._ID);
                        contactBean.setStructuredNameId(data.getString(index));
                    }
                    hasNext = data.moveToNext();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ContactBean queryByPhoneNumber(String phoneNumber) {
        if (!isStringNullOrEmpty(phoneNumber)) {
            phoneNumber = preHandlePhoneNumberPrefix(phoneNumber);
            String selection = ContactsContract.Data.MIMETYPE + "=? AND (" +
                    ContactsContract.CommonDataKinds.Phone.NUMBER + "=? OR "+
                    ContactsContract.CommonDataKinds.Phone.NUMBER + "=? OR "+
                    ContactsContract.CommonDataKinds.Phone.NUMBER + "=? OR "+
                    ContactsContract.CommonDataKinds.Phone.NUMBER + "=?)";
            String[] selectionArgs = new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    phoneNumber,
                    "86" + phoneNumber,
                    "+86" + phoneNumber,
                    "0" + phoneNumber};
            ContentResolver contentResolver = DemoApplication.getApplication().getContentResolver();
            try (Cursor phoneData = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID,
                            ContactsContract.Data.RAW_CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    selection,
                    selectionArgs,
                    null)) {
                if (phoneData != null && phoneData.moveToFirst()) {
                    ContactBean contactBean = new ContactBean();
                    contactBean.addPhoneNumber(phoneData.getString(0), phoneData.getString(2));
                    String rawContactId = phoneData.getString(1);
                    contactBean.setRawContactId(rawContactId);
                    try (Cursor nameData = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                            new String[]{ContactsContract.Data._ID,
                                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME},
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + "=?",
                            new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, rawContactId},
                            null)) {
                        if (nameData != null && nameData.moveToFirst()) {
                            contactBean.setStructuredNameId(nameData.getString(0));
                            contactBean.setDisplayName(nameData.getString(1));
                            contactBean.setFamilyName(nameData.getString(2));
                            contactBean.setGivenName(nameData.getString(3));
                        }
                        return contactBean;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
