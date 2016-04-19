#import <Foundation/Foundation.h>
#import <SystemConfiguration/CaptiveNetwork.h>
#import <Cordova/CDV.h>
#import "ESPTouchTask.h"
#import "ESPTouchResult.h"
#import "ESP_NetUtil.h"
#import "ESPTouchDelegate.h"
#import "AFNetworking.h"
#import "FastSocket.h"

@interface EspTouchDelegateImpl : NSObject<ESPTouchDelegate>

@end

@implementation EspTouchDelegateImpl
NSString *loginID;
CDVInvokedUrlCommand * commandHolder;
NSString *deviceIp ;
NSString *uid;
NSString *userToken ;
NSString *APPId ;
NSString *productKey ;
NSString *token ;
int acitvateTimeout;
NSString* wifiSSID;
NSString* wifiKey;
NSString* activatePort;
NSString* bssid;
NSString *para;
NSString * requestUrl;

NSString* deviceLoginId;
NSString* devicePass;

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

    NSString *loginID;
    CDVInvokedUrlCommand * commandHolder;
    NSString *deviceIp ;
    NSString *uid;
    NSString *userToken ;
    NSString *APPId ;
    NSString *productKey ;
    NSString *token ;
    int acitvateTimeout;
    NSString* wifiSSID;
    NSString* wifiKey;
    NSString* activatePort;
    NSString* bssid;
    FastSocket *socket;
    NSString *para;
    NSString * requestUrl;
    
    NSString* deviceLoginId;
    NSString* devicePass;
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
//
//    if ([command.arguments objectAtIndex:3] == nil || [command.arguments objectAtIndex:4] == nil) {
//        NSLog(@"Error: arguments easylink_version & timeout");
//        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
//        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//        return;
//    }else {
//        easylinkVersion = [[command.arguments objectAtIndex:3] intValue];
//        acitvateTimeout = [[command.arguments objectAtIndex:4] intValue];
//    }

    if (wifiSSID == nil || wifiSSID.length == 0 || wifiKey == nil || wifiKey.length == 0 ) {
        NSLog(@"Error: arguments");
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    // execute the task
//    NSArray *esptouchResultArray = [self executeForResults];
    
    [self.commandDelegate runInBackground:^{
        _esptouchDelegate = [[EspTouchDelegateImpl alloc]init];
        ESPTouchResult *esptouchResult = [self executeForResult];
        NSLog(@"EspTouchDelegateImpl onEsptouchResultAddedWithResult bssid: %@", esptouchResult.bssid);
        deviceIp=[ESP_NetUtil descriptionInetAddrByData:esptouchResult.ipAddrData];
        //
        //    dispatch_async(dispatch_get_main_queue(), ^{
        //
        //    });
        @try {
            
            if (deviceIp!=nil) {
                requestUrl =[[NSString alloc] init];
                //                requestUrl = [[[[[requestUrl stringByAppendingString:@"http://"] stringByAppendingString:deviceIp]
                //                                stringByAppendingString:@":"]
                //                                stringByAppendingString:@"8000"]
                //                               stringByAppendingString:@"/"];
                requestUrl = [requestUrl stringByAppendingString:deviceIp];
                
                para=[[[[[[[[@"{\"app_id\":\"" stringByAppendingString:APPId] stringByAppendingString:@"\",\"product_key\":\""]stringByAppendingString:productKey]stringByAppendingString:@"\",\"user_token\":\""]stringByAppendingString:token] stringByAppendingString:@"\",\"uid\":\""]stringByAppendingString:uid]stringByAppendingString:@"\"}"];
                
                sleep(5);
                
                NSDictionary *ret;
                int resultFlag=0;
                
                socket= [[FastSocket alloc] initWithHost:requestUrl andPort:@"8000"];
                [socket setTimeout:20];
                [socket connect];
                
                NSData *data = [para dataUsingEncoding:NSUTF8StringEncoding];
                long count = [socket sendBytes:[data bytes] count:[data length]];
                
                char bytes[59];
                [socket receiveBytes:bytes count:59];
                NSString *received = [[NSString alloc] initWithBytes:bytes length:59 encoding:NSUTF8StringEncoding];
                
                NSData *jsonData = [received dataUsingEncoding:NSUTF8StringEncoding];
                NSError *err;
                
                
                if(received!=nil){
                    ret = [NSJSONSerialization JSONObjectWithData:jsonData
                                                          options:NSJSONReadingMutableContainers
                                                            error:&err];
                    resultFlag = 1;
                }
                
                
                
                if(resultFlag==1){
                    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:ret];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
                }else{
                    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
                }
            } else {
                CDVPluginResult *
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
            }
        }
        @catch (NSException *exception) {
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
        }
        @finally {
            //
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
- (void)sendDidVerification:(CDVInvokedUrlCommand*)command
{
    NSString* did = [command.arguments objectAtIndex:0];
    commandHolder = command;
    NSString *para=[[@"{\"device_id\":\"" stringByAppendingString:did]stringByAppendingString:@"\"}"];
    NSData *data = [para dataUsingEncoding:NSUTF8StringEncoding];
    long count = [socket sendBytes:[data bytes] count:[data length]];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"OK"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:commandHolder.callbackId];
    
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
    if(socket!=nil)
    {
        [socket close];
    }
    //    easylink_config.delegate = nil;
    //    easylink_config = nil;
}
@end