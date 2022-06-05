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
        deMnlMtcCourseList: any;
    }
}

window.deMnlMtcCourseList = {}

window.deMnlMtcCourseList.initPreview = function(previewDom: HTMLElement) {
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

            const courses = ref<any[]>([]);
            
            const setCourses = (data: any[] | null) => {
                if (!data) {
                    courses.value = [];
                    return;
                }
                courses.value = data;
            }
            
            const apiNode = ref(null);
            
            provideApi(apiNode, { setMessage, setCourses });
            
            return { apiNode, localize, message, courses };
        }
    });
    app.use(JgwcPlugin);
    app.mount(previewDom);
}

JGConsole.registerConletFunction("de.mnl.mtc.conlets.courselist.CourseListConlet",
    "setMessage", function(conletId, message: string) {
    let previewDom = JGConsole.findConletPreview(conletId);
    if (!previewDom) {
        return;
    }
    let api = getApi<any>(previewDom.querySelector
        (":scope .mtc-conlet-courselist-api"));
    api.setMessage(message);
});

JGConsole.registerConletFunction("de.mnl.mtc.conlets.courselist.CourseListConlet",
    "setCourses", function(conletId, courses: [any]) {
    let previewDom = JGConsole.findConletPreview(conletId);
    if (!previewDom) {
        return;
    }
    let api = getApi<any>(previewDom.querySelector
        (":scope .mtc-conlet-courselist-api"));
    api.setMessage(null);
    api.setCourses(courses);
});
