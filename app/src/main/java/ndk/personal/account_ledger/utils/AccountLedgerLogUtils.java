package ndk.personal.account_ledger.utils;

import ndk.personal.account_ledger.constants.ApplicationSpecification;
import ndk.utils_android1.LogUtils1;

public class AccountLedgerLogUtils {

    public static void debug(String message) {

        LogUtils1.debug(ApplicationSpecification.APPLICATION_NAME, message);
    }
}
