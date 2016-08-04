#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import <Cordova/CDV.h>
#import "ESPTouchTask.h"
#import "ESPTouchResult.h"
#import "ESP_NetUtil.h"
#import "ESPTouchDelegate.h"


@interface EspTouchDelegateImpl : NSObject<ESPTouchDelegate>

@end

@implementation EspTouchDelegateImpl

-(void) dismissAlert:(UIAlertView *)alertView
{
    [alertView dismissWithClickedButtonIndex:[alertView cancelButtonIndex] animated:YES];
}

-(void) showAlertWithResult: (ESPTouchResult *) result
{
    NSString *title = nil;
    NSString *message = [NSString stringWithFormat:@"%@ is connected to the wifi" , result.bssid];
    NSTimeInterval dismissSeconds = 3.5;
    UIAlertView *alertView = [[UIAlertView alloc]initWithTitle:title message:message delegate:nil cancelButtonTitle:nil otherButtonTitles:nil];
    [alertView show];
    [self performSelector:@selector(dismissAlert:) withObject:alertView afterDelay:dismissSeconds];
}

-(void) onEsptouchResultAddedWithResult: (ESPTouchResult *) result
{


}
@end

@interface ESPWrapper : CDVPlugin <ESPTouchDelegate> {
    // Member variables go here.


    CDVInvokedUrlCommand * commandHolder;
    NSString *uid;

    NSString *APPId ;
    NSString *productKey ;
    NSString *token ;
    NSString* wifiSSID;
    NSString* wifiKey;
    NSString* activatePort;
    NSString* bssid;

    ESPTouchTask *_esptouchTask;
    EspTouchDelegateImpl *_esptouchDelegate;
}
- (void)setDeviceWifi:(CDVInvokedUrlCommand*)command;
- (void)sendDidVerification:(CDVInvokedUrlCommand*)command;
- (void)dealloc:(CDVInvokedUrlCommand*)command;
@end

@implementation ESPWrapper

-(void)pluginInitialize{

}

- (void)setDeviceWifi:(CDVInvokedUrlCommand*)command
{

    wifiSSID = [command.arguments objectAtIndex:0];
    wifiKey = [command.arguments objectAtIndex:1];
    uid = [command.arguments objectAtIndex:2];
    APPId = [command.arguments objectAtIndex:3];
    productKey = [command.arguments objectAtIndex:4];
    token = [command.arguments objectAtIndex:5];
    commandHolder = command;

    if (wifiSSID == nil || wifiSSID.length == 0 || wifiKey == nil || wifiKey.length == 0 ) {
        NSLog(@"Error: arguments");
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    [self.commandDelegate runInBackground:^{
        _esptouchDelegate = [[EspTouchDelegateImpl alloc]init];
        ESPTouchResult *esptouchResult = [self executeForResult];
        NSLog(@"EspTouchDelegateImpl onEsptouchResultAddedWithResult bssid: %@", esptouchResult.bssid);
        NSString   *deviceIp=[ESP_NetUtil descriptionInetAddrByData:esptouchResult.ipAddrData];
        if (esptouchResult.isSuc) {
            NSDictionary *ret = [NSDictionary dictionaryWithObjectsAndKeys:
                                 esptouchResult.bssid, @"mac",
                                 deviceIp, @"ip",
                                 nil];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:ret];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
        } else {
            CDVPluginResult *
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
        }
    }];

}

- (ESPTouchResult *) executeForResult
{
    NSDictionary *ssidInfo = [self fetchNetInfo];
    NSString *apBssid =[ssidInfo objectForKey:@"BSSID"];
    NSLog(@"bssid: %@", apBssid);
    _esptouchTask =
    [[ESPTouchTask alloc]initWithApSsid:wifiSSID andApBssid:apBssid andApPwd:wifiKey andIsSsidHiden:false];
    // set delegate
    [_esptouchTask setEsptouchDelegate:_esptouchDelegate];
    ESPTouchResult * esptouchResult = [_esptouchTask executeForResult];
    NSLog(@"ESPViewController executeForResult() result is: %@",esptouchResult);
    return esptouchResult;
}

-(void) onEsptouchResultAddedWithResult: (ESPTouchResult *) result
{

}

- (NSDictionary *)fetchNetInfo
{
    NSArray *interfaceNames = CFBridgingRelease(CNCopySupportedInterfaces());
    //    NSLog(@"%s: Supported interfaces: %@", __func__, interfaceNames);

    NSDictionary *SSIDInfo;
    for (NSString *interfaceName in interfaceNames) {
        SSIDInfo = CFBridgingRelease(
                                     CNCopyCurrentNetworkInfo((__bridge CFStringRef)interfaceName));
        //        NSLog(@"%s: %@ => %@", __func__, interfaceName, SSIDInfo);

        BOOL isNotEmpty = (SSIDInfo.count > 0);
        if (isNotEmpty) {
            break;
        }
    }
    return SSIDInfo;
}

- (void)dealloc:(CDVInvokedUrlCommand*)command
{
    NSLog(@"//====dealloc...====");
    if (_esptouchTask !=nil) {
        [_esptouchTask interrupt];
    }
}
@end