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

import javax.annotation.Resource;

import org.hibnet.jst.poc.jst.PageLayoutTemplateFactory;
import org.hibnet.jst.poc.jst.PageLayoutV2TemplateFactory;
import org.hibnet.jst.poc.jst.Template;
import org.hibnet.jst.poc.model.User;

public abstract class AbstractTemplate {

    @Resource
    PageLayoutTemplateFactory pageLayoutTemplateFactory;

    @Resource
    PageLayoutV2TemplateFactory pageLayoutV2TemplateFactory;

    public AbstractPageLayoutTemplate getPageTemplate(Template contentTemplate, User connectedUser) {
        if (connectedUser != null && connectedUser.isV2) {
            return pageLayoutV2TemplateFactory.build(connectedUser, contentTemplate);
        }
        return pageLayoutTemplateFactory.build(connectedUser, contentTemplate);
    }
}
