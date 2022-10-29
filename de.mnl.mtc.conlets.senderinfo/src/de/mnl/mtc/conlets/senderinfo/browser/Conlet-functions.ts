/*
 * Moodle Tools Console
 * Copyright (C) 2022 Michael N. Lipp
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 */

import { reactive, ref, createApp, computed, onMounted } from "vue";
import JGConsole from "jgconsole"
import JgwcPlugin, { JGWC } from "jgwc";
import { provideApi, getApi } from "aash-plugin";
import l10nBundles from "l10nBundles";

// For global access
declare global {
    interface Window {
        deMnlMtcSenderInfo: any;
    }
}

window.deMnlMtcSenderInfo = {}

window.deMnlMtcSenderInfo.initPreview = function(previewDom: HTMLElement) {
    const conletId = (<HTMLElement>previewDom.closest("*[data-conlet-id]")!)
        .dataset["conletId"]!;
    let app = createApp({
        setup() {
            const localize = (key: string) => {
                return JGConsole.localize(
                    l10nBundles, JGWC.lang()!, key);
            };

            const message = ref(null);
            
            const setMessage = (msg: string) => {
                message.value = msg;
            }

            const items = ref<any[]>([]);
            
            const setItems = (data: any[] | null) => {
                if (!data) {
                    items.value = [];
                    return;
                }
                items.value = data;
            }

            const showDetails = (addr: string) => {
                // JGConsole.notifyConletModel(conletId, "showDetails", addr);
                // addConlet(conletType: string, renderModes: RenderMode[], properties?: null | Map<string, string>): void
                let props = new Map();
                props.set("address", addr);
                JGConsole.instance.addConlet(
                    "de.mnl.mtc.conlets.senderinfo.SenderInfoConlet", 
                    [JGConsole.RenderMode.View], props);
            }
            
            const apiNode = ref(null);
            
            provideApi(apiNode, { setMessage, setItems });
            
            return { apiNode, localize, message, items, showDetails };
        }
    });
    app.use(JgwcPlugin);
    app.mount(previewDom);
}

window.deMnlMtcSenderInfo.initView = function(previewDom: HTMLElement) {
    const conletId = (<HTMLElement>previewDom.closest("*[data-conlet-id]")!)
        .dataset["conletId"]!;
    let app = createApp({
        setup() {
            const localize = (key: string) => {
                return JGConsole.localize(
                    l10nBundles, JGWC.lang()!, key);
            };

            const message = ref(null);
            
            const setMessage = (msg: string) => {
                message.value = msg;
            }

            const userInfo = ref(null);
            
            const setUserInfo = (info: any) => {
                userInfo.value = info;
            }

            const apiNode = ref(null);
            
            provideApi(apiNode, { setMessage, setUserInfo });
            
            return { apiNode, localize, message, userInfo };
        }
    });
    app.use(JgwcPlugin);
    app.mount(previewDom);
}

JGConsole.registerConletFunction("de.mnl.mtc.conlets.senderinfo.SenderInfoConlet",
    "setMessage", function(conletId, message: string) {
    let conlet = JGConsole.findConletPreview(conletId);
    if (!conlet) {
        conlet = JGConsole.findConletView(conletId);
        if (!conlet) {
            return;
        }
    }
    let api = getApi<any>(conlet.element().querySelector
        (":scope .mtc-conlet-senderinfo-api"));
    api.setMessage(message);
});

JGConsole.registerConletFunction("de.mnl.mtc.conlets.senderinfo.SenderInfoConlet",
    "setItems", function(conletId, items: [any]) {
    let previewConlet = JGConsole.findConletPreview(conletId);
    if (!previewConlet) {
        return;
    }
    let api = getApi<any>(previewConlet.element().querySelector
        (":scope .mtc-conlet-senderinfo-api"));
    api.setMessage(null);
    api.setItems(items);
});

JGConsole.registerConletFunction("de.mnl.mtc.conlets.senderinfo.SenderInfoConlet",
    "setUserInfo", function(conletId, userInfo: any) {
    let conlet = JGConsole.findConletView(conletId);
    if (!conlet) {
        return;
    }
    let api = getApi<any>(conlet.element().querySelector
        (":scope .mtc-conlet-senderinfo-api"));
    api.setMessage(null);
    api.setUserInfo(userInfo);
});
