import { PermissionsAndroid, Platform } from 'react-native';

export const requestPermissions = async (permissionDialogTitle, permissionDialogMessage) => {
    if (Platform.OS === 'android') {
        const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.WRITE_EXTERNAL_STORAGE, {
            title: permissionDialogTitle,
            message: permissionDialogMessage,
        });

        // On devices before SDK version 23, the permissions are automatically granted if they appear in the manifest,
        // so check and request should always be true.
        // https://github.com/facebook/react-native-website/blob/master/docs/permissionsandroid.md
        const isAuthorized = Platform.Version >= 23 ? granted === PermissionsAndroid.RESULTS.GRANTED : granted === true;
        return isAuthorized;
    }
    return true;
}