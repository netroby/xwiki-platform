/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.observation.event;

import org.xwiki.observation.event.filter.EventFilter;


/**
 * An event triggered right after evaluation of a script macro (Groovy, Velocity, etc.) was finished (no matter
 * successfully or not).
 * <p>
 * This event is supposed to be sent with {@link org.xwiki.rendering.transformation.MacroTransformationContext} as the
 * source and {@link org.xwiki.rendering.macro.script.ScriptMacroParameters} as data.</p>
 * 
 * @version $Id$
 * @since 2.5M1
 * @see ScriptEvaluationStartsEvent
 */
public class ScriptEvaluationFinishedEvent extends AbstractFilterableEvent
{
    /** Serial version ID. Increment only if the <i>serialized</i> version of this class changes. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializing the event filter with an
     * {@link org.xwiki.observation.event.filter.AlwaysMatchingEventFilter}, meaning that this event will match any
     * other event of the same type.
     */
    public ScriptEvaluationFinishedEvent()
    {
    }

    /**
     * Constructor initializing the event filter with a {@link org.xwiki.observation.event.filter.FixedNameEventFilter},
     * meaning that this event will match only events of the same type affecting the same passed name.
     *
     * @param scriptMacroName name of the macro to match, e.g. "velocity"
     */
    public ScriptEvaluationFinishedEvent(String scriptMacroName)
    {
        super(scriptMacroName);
    }

    /**
     * Constructor using a custom {@link EventFilter}.
     *
     * @param eventFilter the filter to use for matching events
     */
    public ScriptEvaluationFinishedEvent(EventFilter eventFilter)
    {
        super(eventFilter);
    }
}

