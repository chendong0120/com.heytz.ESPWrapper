var exec = require('cordova/exec');

exports.setDeviceWifi = function (wifiSSID,
                                  wifiKey,
                                  uid,
                                  appid,
                                  productKey,
                                  token,
                                  moduleDefaultUser,
                                  moduleDefaultPass,
                                  success, error) {
    exec(success, error, "ESPWrapper", "setDeviceWifi",
        [
            wifiSSID,
            wifiKey,
            uid,
            appid,
            productKey,
            token,
            moduleDefaultUser,
            moduleDefaultPass
        ]);
};
exports.sendDidVerification = function (did,
                                        success, error) {
    exec(success, error, "ESPWrapper", "sendDidVerification",
        [
            did
        ]);
};
exports.dealloc = function () {
    exec( null,null,"ESPWrapper", "dealloc",
        []);
};
