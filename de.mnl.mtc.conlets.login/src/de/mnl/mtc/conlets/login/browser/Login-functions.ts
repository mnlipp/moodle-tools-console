/*
 * JGrapes Event Driven Framework
 * Copyright (C) 2022 Michael N. Lipp
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Affero General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along 
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
        deMnlMtcLogin: any;
    }
}

window.deMnlMtcLogin = {}

interface AccountData {
    website: string;
    username: string;
    password: string;
}

window.deMnlMtcLogin.openDialog 
    = function(dialogDom: HTMLElement, isUpdate: boolean) {
    if (isUpdate) {
        return;
    }
    let app = createApp({
        setup() {
            const formId = (<HTMLElement>dialogDom
                .closest("*[data-conlet-id]")!).id + "-form";

            const accountData: AccountData = reactive({
                website: "",
                username: "",
                password: ""
            });

            const localize = (key: string) => {
                return JGConsole.localize(
                    l10nBundles, JGWC.lang()!, key);
            };

            const info = ref(null);
            const warning = ref(null);
            
            const setMessages = (infoMsg: string, warningMsg: string) => {
                info.value = infoMsg;
                warning.value = warningMsg;
            }

            const formDom = ref(null);

            provideApi(formDom, { accountData, setMessages });
                        
            return { formDom, formId, localize, accountData, info, warning };
        },
        template: `
          <form :id="formId" ref="formDom" onsubmit="return false;"
            class="mtc-conlet-login-form">
            <fieldset>
              <legend>{{ localize("Login Data") }}</legend>
              <p>
                <label class="form__label--full-width">
                  <span>
                    {{ localize("Website") }}
                    <strong>
                      <abbr v-bind:title='localize("required_website")'>*</abbr>
                    </strong>
                  </span>
                  <input type="text" name="url" v-model="accountData.website"
                    autocomplete="section-moodle url">
                </label>
              </p>
              <p>
                <label class="form__label--full-width">
                  <span>
                    {{ localize("User Name") }}
                    <strong>
                      <abbr v-bind:title='localize("required")'>*</abbr>
                    </strong>
                  </span>
                  <input type="text" name="username" v-model="accountData.username"
                    autocomplete="section-moodle username">
                </label>
              </p>
              <p>
                <label class="form__label--full-width">
                  <span>
                    {{ localize("Password") }}
                    <strong>
                      <abbr v-bind:title='localize("required")'>*</abbr>
                    </strong>
                  </span>
                  <input type="password" name="password" v-model="accountData.password"
                    autocomplete="section-moodle current-password">
                </label>
              </p>
              <p v-if="info" class="mtc-conlet-login-form__info">
                {{ info }}
              </p>
              <p v-if="warning" class="mtc-conlet-login-form__warning">
                {{ warning }}
              </p>
            </fieldset>
          </form>`
    });
    app.use(JgwcPlugin);
    app.mount(dialogDom);
}

window.deMnlMtcLogin.apply = function(dialogDom: HTMLElement,
    apply: boolean, close: boolean) {
    const conletId = (<HTMLElement>dialogDom.closest("[data-conlet-id]")!)
        .dataset["conletId"]!;
    const accountData = getApi<any>
        (dialogDom.querySelector(":scope form")!)!.accountData;
    JGConsole.notifyConletModel(conletId, "loginData", 
        accountData.website, accountData.username, accountData.password);
    return;
}

JGConsole.registerConletFunction("de.mnl.mtc.conlets.login.LoginConlet",
    "setMessages", function(conletId, info, warning) {
    let api = getApi<any>(document.querySelector
        (".conlet-modal-dialog[data-conlet-id=\"" + conletId + "\"] form"));
    api.setMessages(info, warning);
});
