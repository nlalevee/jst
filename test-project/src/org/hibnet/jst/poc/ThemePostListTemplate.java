/*
 *  Copyright 2013 JST contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hibnet.jst.poc;

import org.hibnet.jst.poc.jst.PostListTemplate;
import org.hibnet.jst.poc.jst.PostTemplate;
import org.hibnet.jst.poc.jst.ThemeLayoutTemplate;
import org.hibnet.jst.poc.model.Theme;
import org.hibnet.jst.poc.model.User;

public class ThemePostListTemplate extends AbstractTemplate {

    public void render(User connectedUser, Theme t) {
        PostTemplate postTemplate = new PostTemplate(null);
        PostListTemplate postListTemplate = new PostListTemplate(t.posts, postTemplate);
        ThemeLayoutTemplate themeLayoutTemplate = new ThemeLayoutTemplate(postListTemplate, t);
        AbstractPageLayoutTemplate pageLayoutTemplate = getPageTemplate(themeLayoutTemplate, connectedUser);
        pageLayoutTemplate.render();
    }

}
