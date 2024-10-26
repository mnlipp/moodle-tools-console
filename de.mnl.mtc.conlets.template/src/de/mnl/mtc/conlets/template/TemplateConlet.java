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

package de.mnl.mtc.conlets.template;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jgrapes.core.Channel;
import org.jgrapes.core.Event;
import org.jgrapes.core.Manager;
import org.jgrapes.core.annotation.Handler;
import org.jgrapes.webconsole.base.Conlet.RenderMode;
import org.jgrapes.webconsole.base.ConletBaseModel;
import org.jgrapes.webconsole.base.ConsoleConnection;
import org.jgrapes.webconsole.base.WebConsoleUtils;
import org.jgrapes.webconsole.base.events.AddConletRequest;
import org.jgrapes.webconsole.base.events.AddConletType;
import org.jgrapes.webconsole.base.events.AddPageResources.ScriptResource;
import org.jgrapes.webconsole.base.events.ConsoleReady;
import org.jgrapes.webconsole.base.events.RenderConlet;
import org.jgrapes.webconsole.base.events.RenderConletRequestBase;
import org.jgrapes.webconsole.base.events.SetLocale;
import org.jgrapes.webconsole.base.freemarker.FreeMarkerConlet;

/**
 * A conlet for listing courses.
 */
public class TemplateConlet
    extends FreeMarkerConlet<TemplateConlet.CourseListModel> {

  private static final Set<RenderMode> MODES
    = RenderMode.asSet(RenderMode.Preview);

  /**
   * Creates a new component with its channel set to the given channel.
   * 
   * @param componentChannel the channel that the component's handlers listen
   *            on by default and that {@link Manager#fire(Event, Channel...)}
   *            sends the event to
   */
  public TemplateConlet(Channel componentChannel) {
    super(componentChannel);
  }

  /**
   * Register conlet.
   *
   * @param event the event
   * @param channel the channel
   * @throws TemplateNotFoundException the template not found exception
   * @throws MalformedTemplateNameException the malformed template name exception
   * @throws ParseException the parse exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Handler
  public void onConsoleReady(ConsoleReady event, ConsoleConnection channel)
      throws TemplateNotFoundException, MalformedTemplateNameException,
      ParseException, IOException {
    // Add conlet resources to page
    channel.respond(new AddConletType(type())
      .addRenderMode(RenderMode.Preview).setDisplayNames(
        localizations(channel.supportedLocales(), "conletName"))
      .addScript(new ScriptResource()
        .setScriptUri(event.renderSupport().conletResource(
          type(), "Conlet-functions.js"))
        .setScriptType("module"))
      .addCss(event.renderSupport(),
        WebConsoleUtils.uriFromPath("Conlet-style.css")));
  }

  @Override
  protected String generateInstanceId(AddConletRequest event,
      ConsoleConnection channel) {
    return "Singleton";
  }

  @Override
  protected Optional<CourseListModel> createNewState(AddConletRequest event,
      ConsoleConnection channel, String conletId) throws Exception {
    return Optional
      .ofNullable(stateFromSession(channel.session(), conletId)
        .orElse(super.createNewState(event, channel, conletId).get()));
  }

  @Override
  protected Optional<CourseListModel> createStateRepresentation(Event<?> event,
      ConsoleConnection channel, String conletId) throws IOException {
    return Optional.of(new CourseListModel(conletId));
  }

  @Override
  protected Set<RenderMode> doRenderConlet(RenderConletRequestBase<?> event,
      ConsoleConnection channel, String conletId,
      CourseListModel conletState) throws Exception {
    Set<RenderMode> renderedAs = new HashSet<>(event.renderAs());
    if (event.renderAs().contains(RenderMode.Preview)) {
      Template tpl
        = freemarkerConfig().getTemplate("Conlet-preview.ftl.html");
      channel.respond(new RenderConlet(type(), conletId,
        processTemplate(event, tpl,
          fmModel(event, channel, conletId, conletState)))
            .setRenderAs(
              RenderMode.Preview.addModifiers(event.renderAs()))
            .setSupportedModes(MODES));
      renderedAs.add(RenderMode.Preview);
    }
    return renderedAs;
  }

  @Override
  protected boolean doSetLocale(SetLocale event, ConsoleConnection channel,
      String conletId) throws Exception {
    return true;
  }

  /**
   * Model with no additional info.
   */
  public static class CourseListModel extends ConletBaseModel {

    /**
     * Creates a new model with the given type and id.
     * 
     * @param conletId the web console component id
     */
    @ConstructorProperties({ "conletId" })
    public CourseListModel(String conletId) {
      super(conletId);
    }

  }

}
