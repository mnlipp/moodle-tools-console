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
        deMnlMtcTobegraded: any;
    }
}

window.deMnlMtcTobegraded = {}

window.deMnlMtcTobegraded.initPreview = function(previewDom: HTMLElement) {
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
            
            const groupsInAssignment 
                = new Map<number,Map<number,any>>();
                
            const groupsByAssignment = (assignmentId: number) => {
                let groups = [...groupsInAssignment.get(assignmentId)!.values()];
                return groups.sort((a, b) => a.name.localeCompare(b.name));
            }
            
            const setCourses = (data: any[] | null) => {
                if (!data) {
                    courses.value = [];
                    return;
                }
                for (let course of data) {
                    for (let assignment of course.assignments) {
                        for (let user of assignment.users) {
                            for (let group of user.groups) {
                                if (!groupsInAssignment.has(assignment.id)) {
                                    groupsInAssignment.set(assignment.id,
                                        new Map<number,any>());
                                }
                                let groups = groupsInAssignment.get(assignment.id)!;
                                groups.set(group.id, group);
                            }
                        }
                    }
                }                
                courses.value = data;
            }
            
            const groupUrl = (assignment: any, group: any) => {
                return assignment.url + "&group=" + group.id;
            }
            
            const apiNode = ref(null);
            
            provideApi(apiNode, { setMessage, setCourses });
            
            return { apiNode, localize, message, courses, groupsByAssignment,
                groupUrl };
        }
    });
    app.use(JgwcPlugin);
    app.mount(previewDom);
}

JGConsole.registerConletFunction("de.mnl.mtc.conlets.tobegraded.ToBeGradedConlet",
    "setMessage", function(conletId, message: string) {
    let previewDom = JGConsole.findConletPreview(conletId);
    if (!previewDom) {
        return;
    }
    let api = getApi<any>(previewDom.querySelector
        (":scope .mtc-conlet-tobegraded-api"));
    api.setMessage(message);
});

JGConsole.registerConletFunction("de.mnl.mtc.conlets.tobegraded.ToBeGradedConlet",
    "setPreviewData", function(conletId, courses: [any]) {
    let previewDom = JGConsole.findConletPreview(conletId);
    if (!previewDom) {
        return;
    }
    let api = getApi<any>(previewDom.querySelector
        (":scope .mtc-conlet-tobegraded-api"));
    api.setMessage(null);
    api.setCourses(courses);
});


