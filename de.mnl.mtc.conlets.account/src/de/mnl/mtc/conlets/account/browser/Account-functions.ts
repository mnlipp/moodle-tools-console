/*
 * Moodle Tools Console
 * Copyright (C) 2022 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

import { reactive, ref, createApp, computed, onMounted } from "vue";
import JGConsole from "jgconsole"
import JgwcPlugin, { JGWC } from "jgwc";
import { provideApi, getApi } from "aash-plugin";
import l10nBundles from "l10nBundles";

// For global access
declare global {
    interface Window {
        deMnlMtcAccount: any;
    }
}

window.deMnlMtcAccount = {}

interface AccountData {
    website: string;
    username: string;
    password: string;
}

let accountData: AccountData = reactive({
    website: "",
    username: "",
    password: ""
});

window.deMnlMtcAccount.initPreview = function(previewDom: HTMLElement) {
    let app = createApp({
        setup() {
            const submitData = (event: Event) => {
                let conletId = previewDom
                    .closest("[data-conlet-id]")!.getAttribute("data-conlet-id")!;
                JGConsole.notifyConletModel(conletId, "accountData", 
                    accountData.website, accountData.username, accountData.password);
            };
            
            const localize = (key: string) => {
                return JGConsole.localize(
                    l10nBundles, JGWC.lang()!, key);
            };
                        
            return { localize, submitData, accountData };
        }
    });
    app.use(JgwcPlugin);
    app.mount(previewDom);
}

JGConsole.registerConletFunction(
    "de.mnl.mtc.conlets.account.AccountConlet",
    "accountData", function(conletId: string, 
        website: string, username: string) {
        accountData.website = website;
        accountData.username = username;
    });
