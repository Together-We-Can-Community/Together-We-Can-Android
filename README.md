# Together We Can - Community

![](https://storage.googleapis.com/arimac-storage/together-we-can-covid/together-wc-logo.png)

Together we can is a mobile application that is robust, highly secure, extensible and privacy protected was developed to track primary, secondary and tertiary contacts of newly identified cases using the Bluetooth technology and thereby inform authorities with the potentially exposed citizens of Sri Lanka.

Additionally, citizens can update their health status (such as not infected/ quarantined) and request assistance (food, medicine or nearby cases) at their convenience without the fear of stigma while protecting the nation as a whole.

The application will reach its highest benefit when the entire society uses the app collaboratively.

## Configuration - Environment and Tools

1. Android Studio 3.6
2. JAVA v1.8
3. Android SDK 29 with Build tools v29.0.2

## Required code level changes

1. Below UUIDs in com.arimaclanka.android.wecan.bluetooth.Consts class should be changed. Use a uuid generaor like [UUID Generator](https://www.uuidgenerator.net/)

    ```
    SERVICE_UUID
    READ_CHARACTERISTIC_UUID 
    WRITE_CHARACTERISTIC_UUID
    ```
    
2. Setup OneSignal account and add onesignal_app_id in build.gradle (Optional)
3. Setup firebase project and add valid google-services.json file to setup firebase remote configs (optional)

Things to notice,
All backend API calls are removed from this source. In order to ensure application flow works without an issue, Api calls (Indicated by  `//API Call: comments`) are simulated using timed handlers.

### Read More

* [Technical Documentation](https://site.togetherwecanlk.com/tech/)
* [White Paper](https://site.togetherwecanlk.com/tech/)
* [Pivacy Statement](https://site.togetherwecanlk.com/privacy/)

### Contact 

* Arimac Lanka PVT LTD | hello@arimaclanka.com

### Changelog

2020-05-12
* Initial release of the repo
