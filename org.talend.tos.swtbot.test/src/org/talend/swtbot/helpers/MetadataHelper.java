// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.swtbot.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swtbot.eclipse.gef.finder.SWTGefBot;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.Assert;
import org.talend.swtbot.TalendSwtBotForTos;
import org.talend.swtbot.Utilities;
import org.talend.swtbot.items.TalendFileItem;
import org.talend.swtbot.items.TalendMetadataItem;

/**
 * DOC fzhong class global comment. Detailled comment
 */
public class MetadataHelper {

    private static SWTGefBot gefBot = new SWTGefBot();

    private static TalendSwtBotForTos util = new TalendSwtBotForTos();

    public static void output2Console(SWTBotGefEditor jobEditor, TalendMetadataItem item) {
        Utilities.dndMetadataOntoJob(gefBot, jobEditor, item.getItem(), item.getComponentType(), new Point(100, 100));
        Utilities.dndPaletteToolOntoJob(gefBot, jobEditor, "tLogRow", new Point(300, 100));

        SWTBotGefEditPart metadata = util.getTalendComponentPart(jobEditor, item.getItemName());
        Assert.assertNotNull("can not get component '" + item.getComponentType() + "'", metadata);
        jobEditor.select(metadata).setFocus();
        jobEditor.clickContextMenu("Row").clickContextMenu("Main");
        SWTBotGefEditPart tlogRow = util.getTalendComponentPart(jobEditor, "tLogRow_1");
        Assert.assertNotNull("can not get component 'tLogRow'", tlogRow);
        jobEditor.click(tlogRow);
        jobEditor.save();

        String[] array = jobEditor.getTitle().split(" ");
        String jobName = array[1];
        // String jobVersion = array[2];
        gefBot.viewByTitle("Run (Job " + jobName + ")").setFocus();
        gefBot.button(" Run").click();

        // gefBot.waitUntil(Conditions.shellIsActive("Launching " + jobName + " " + jobVersion));
        // gefBot.waitUntil(Conditions.shellCloses(gefBot.shell("Launching " + jobName + " " + jobVersion)));

        gefBot.waitUntil(new DefaultCondition() {

            public boolean test() throws Exception {
                return gefBot.button(" Run").isEnabled();
            }

            public String getFailureMessage() {
                return "job did not finish running";
            }
        }, 10000);
    }

    public static void assertResult(String result, TalendMetadataItem item) throws IOException, URISyntaxException {
        StringBuffer source = new StringBuffer();
        File sourcefile = ((TalendFileItem) item).getSourceFileAsResult();
        BufferedReader reader = new BufferedReader(new FileReader(sourcefile));
        String tempStr = null;
        while ((tempStr = reader.readLine()) != null)
            source.append(tempStr + "\n");
        if (result.indexOf(source.toString()) == -1)
            Assert.fail("the results are not expected");
    }
}