var exec = require('cordova/exec');

exports.setDeviceWifi = function (wifiSSID,
                                  wifiKey,
                                  username,
                                  easylinkVersion,
                                  activateTimeout,
                                  activatePort,
                                  moduleDefaultUser,
                                  moduleDefaultPass,
                                  success, error) {
    exec(success, error, "ESPWrapper", "setDeviceWifi",
        [
            wifiSSID,
            wifiKey,
            username,
            easylinkVersion,
            activateTimeout,
            activatePort,
            moduleDefaultUser,
            moduleDefaultPass
        ]);
};
exports.dealloc = function () {
    exec( null,null,"mxsdkwrapper", "dealloc",
        []);
};
