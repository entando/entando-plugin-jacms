/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.aps.system.services.content.model.attribute;

import com.agiletec.aps.system.common.entity.model.AttributeFieldError;
import com.agiletec.aps.system.common.entity.model.AttributeTracer;
import com.agiletec.aps.system.common.entity.model.attribute.HypertextAttribute;
import com.agiletec.aps.system.services.lang.ILangManager;
import com.agiletec.aps.system.services.lang.Lang;
import com.agiletec.aps.system.services.page.IPageManager;
import com.agiletec.plugins.jacms.aps.system.services.content.IContentManager;
import com.agiletec.plugins.jacms.aps.system.services.content.model.CmsAttributeReference;
import com.agiletec.plugins.jacms.aps.system.services.content.model.Content;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SymbolicLink;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.util.HypertextAttributeUtil;
import com.agiletec.plugins.jacms.aps.system.services.content.model.attribute.util.SymbolicLinkValidator;
import com.agiletec.plugins.jacms.aps.system.services.resource.IResourceManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.entando.entando.ent.util.EntLogging.EntLogFactory;
import org.entando.entando.ent.util.EntLogging.EntLogger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Rappresenta una informazione di tipo "ipertesto" specifico per il cms.
 *
 * @author W.Ambu - E.Santoboni
 */
public class CmsHypertextAttribute extends HypertextAttribute implements IReferenceableAttribute {

    private static final EntLogger logger = EntLogFactory.getSanitizedLogger(CmsHypertextAttribute.class);

    @Override
    public Object getAttributePrototype() {
        CmsHypertextAttribute prototype = (CmsHypertextAttribute) super.getAttributePrototype();
        prototype.setContentManager(this.getContentManager());
        prototype.setResourceManager(this.getResourceManager());
        prototype.setPageManager(this.getPageManager());
        return prototype;
    }

    /**
     * Restituisce il testo con modificato con eliminate l'apertura del primo
     * paragrafo e la chiusura dell'ultimo.
     *
     * @return Il testo modificato.
     */
    public String getTextPLess() {
        String masterText = this.getText();
        String text = masterText.replaceFirst("<p>", "");
        StringBuilder sbuffer = new StringBuilder(text);
        int lastIndexOfP = sbuffer.lastIndexOf("</p>");
        if (lastIndexOfP != -1) {
            sbuffer.replace(lastIndexOfP, lastIndexOfP + 4, "");
        }
        return sbuffer.toString();
    }

    /**
     * Restituisce la porzione di testo totale antecedente ad una eventuale
     * immagine da inserire internamente all'ipertesto. Il testo viene ricavato
     * dal testo principale la cui fine è corrispondente all'inizio del
     * paragrafo (apertura inclusa) più vicino al punto del testo completo
     * ricavato dalla percentuale specificata.
     *
     * @param percent La percentuale, rispetto all'inizio del testo, rispetto al
     * quale ricavare il punto di taglio.
     * @return La porzione di testo totale antecedente ad una eventuale
     * immagine.
     */
    public String getTextBeforeImage(int percent) {
        String text = super.getText();
        int cutPoint = HypertextAttributeUtil.getIndexCutPoint(text, percent);
        String textBefore = text.substring(0, cutPoint);
        return textBefore;
    }

    /**
     * Restituisce la porzione di testo totale successivo ad una eventuale
     * immagine da inserire internamente all'ipertesto. Il testo viene ricavato
     * dal testo principale il cui inizio è corrispondente all'inizio del
     * paragrafo (apertura esclusa) più vicino al punto del testo completo
     * ricavato dalla percentuale specificata.
     *
     * @param percent La percentuale, rispetto all'inizio del testo, rispetto al
     * quale ricavare il punto di taglio.
     * @return La porzione di testo totale successiva ad una eventuale immagine.
     */
    public String getTextAfterImage(int percent) {
        String text = super.getText();
        int cutPoint = HypertextAttributeUtil.getIndexCutPoint(text, percent);
        String textAfter = text.substring(cutPoint);
        return textAfter;
    }

    /**
     * Restituisce la porzione di testo totale interposto tra due eventuali
     * immagini da inserire internamente all'ipertesto. Il testo viene ricavato
     * dal testo principale il cui inizio è corrispondente all'inizio del
     * paragrafo (apertura esclusa) più vicino al punto del testo completo
     * ricavato dalla percentuale start specificata, e la cui fine è
     * corrispondente all'inizio del paragrafo (apertura inclusa) più vicina al
     * punto del testo completo ricavato dalla percentuale percentEnd
     * specificata.
     *
     * @param percentStart La percentuale, rispetto all'inizio del testo,
     * rispetto al quale ricavare il punto di taglio iniziale.
     * @param percentEnd La percentuale, rispetto all'inizio del testo, rispetto
     * al quale ricavare il punto di taglio finale.
     * @return La porzione di testo totale interposto tra due eventuali
     * immagini.
     */
    public String getTextByRange(int percentStart, int percentEnd) {
        String text = super.getText();
        int firstCutPoint = HypertextAttributeUtil.getIndexCutPoint(text, percentStart);
        int endCutPoint = HypertextAttributeUtil.getIndexCutPoint(text, percentEnd);
        String textByRange = text.substring(firstCutPoint, endCutPoint);
        return textByRange;
    }

    @Override
    public List<CmsAttributeReference> getReferences(List<Lang> systemLangs) {
        List<CmsAttributeReference> refs = new ArrayList<>();
        for (Lang lang : systemLangs) {
            String text = this.getTextMap().get(lang.getCode());
            List<SymbolicLink> links = HypertextAttributeUtil.getSymbolicLinksOnText(text);
            if (null != links && !links.isEmpty()) {
                for (SymbolicLink symbLink : links) {
                    if (symbLink.getDestType() != SymbolicLink.URL_TYPE) {
                        CmsAttributeReference ref = new CmsAttributeReference(symbLink.getPageDest(),
                                symbLink.getContentDest(), symbLink.getResourceDest());
                        refs.add(ref);
                    }
                }
            }
        }
        return refs;
    }

    @Override
    @Deprecated
    public List<AttributeFieldError> validate(AttributeTracer tracer, ILangManager langManager) {
        return this.validate(tracer, langManager, null);
    }

    @Override
    public List<AttributeFieldError> validate(AttributeTracer tracer, ILangManager langManager, BeanFactory beanFactory) {
        List<AttributeFieldError> errors = super.validate(tracer, langManager, beanFactory);
        try {
            List<Lang> langs = langManager.getLangs();
            for (Lang lang : langs) {
                AttributeTracer textTracer = (AttributeTracer) tracer.clone();
                textTracer.setLang(lang);
                String text = this.getTextMap().get(lang.getCode());
                if (null == text) {
                    continue;
                }
                List<SymbolicLink> links = HypertextAttributeUtil.getSymbolicLinksOnText(text);
                if (null != links && !links.isEmpty()) {
                    for (SymbolicLink symbLink : links) {
                        SymbolicLinkValidator sler = this.getSymbolicLinkValidator(beanFactory);
                        AttributeFieldError attributeError = sler.scan(symbLink, (Content) this.getParentEntity());
                        if (null != attributeError) {
                            AttributeFieldError error = new AttributeFieldError(this, attributeError.getErrorCode(), textTracer);
                            if (attributeError.getMessage() == null) {
                                attributeError.setMessage("Invalid link - page " + symbLink.getPageDest()
                                        + " - content " + symbLink.getContentDest() + " - Error code " + attributeError.getErrorCode());
                            }
                            error.setMessage(attributeError.getMessage());
                            errors.add(error);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("Error validating Attribute '{}'", this.getName(), t);
            throw new RuntimeException("Error validating Attribute '" + this.getName() + "'", t);
        }
        return errors;
    }

    private SymbolicLinkValidator getSymbolicLinkValidator(BeanFactory beanFactory) {
        return new SymbolicLinkValidator(
                beanFactory == null ? this.contentManager : beanFactory.getBean(IContentManager.class),
                beanFactory == null ? this.pageManager : beanFactory.getBean(IPageManager.class),
                beanFactory == null ? this.resourceManager : beanFactory.getBean(IResourceManager.class)
        );
    }

    @Deprecated
    protected IContentManager getContentManager() {
        return contentManager;
    }

    @Deprecated
    public void setContentManager(IContentManager contentManager) {
        this.contentManager = contentManager;
    }

    @Deprecated
    protected IPageManager getPageManager() {
        return pageManager;
    }

    @Deprecated
    public void setPageManager(IPageManager pageManager) {
        this.pageManager = pageManager;
    }

    @Deprecated
    public IResourceManager getResourceManager() {
        return resourceManager;
    }

    @Deprecated
    public void setResourceManager(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public boolean isSearchableOptionSupported() {
        return false;
    }

    private transient IContentManager contentManager;
    private transient IPageManager pageManager;
    private transient IResourceManager resourceManager;

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        WebApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
        if (ctx == null) {
            logger.warn("Null WebApplicationContext during deserialization");
            return;
        }
        this.contentManager = ctx.getBean(IContentManager.class);
        this.pageManager = ctx.getBean(IPageManager.class);
        this.resourceManager = ctx.getBean(IResourceManager.class);
        this.setLangManager(ctx.getBean(ILangManager.class));
    }
}
