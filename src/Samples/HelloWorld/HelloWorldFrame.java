//**************************************************************************
//	Copyright (C), 1999-2001 Adaptinet Inc.
//
//   THIS PROGRAM IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND OR MODIFY
//   IT UNDER THE TERMS OF THE GNU GENERAL PUBLIC LICENSE AS PUBLISHED BY
//   THE FREE SOFTWARE FOUNDATION; EITHER VERSION 2 OF THE LICENSE, OR
//   ANY LATER VERSION.
//
//   THIS SOFTWARE IS PROVIDED BY ADAPTINET AND CONTRIBUTORS
//   ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
//   NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
//   FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
//   ADAPTINET OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
//   INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
//   STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//   OF THE POSSIBILITY OF SUCH DAMAGE.
//
//   IF YOU HAVE NOT RECEIVED A COPY OF THE GNU GENERAL PUBLIC LICENSE
//   WITH THIS PROGRAM WRITE TO THE FREE SOFTWARE FOUNDATION, INC., AT
//   59 TEMPLE PLACE, SUITE 330, BOSTON, MA 02111-1307 USA
//
//**************************************************************************
package Samples.HelloWorld;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.adaptinet.pluginagent.Plugin;

public class HelloWorldFrame extends JFrame
{
	private static final long serialVersionUID = 1L;

	Plugin plugin = null;

    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton jButtonSayHello = new JButton();
    FlowLayout flowLayout1 = new FlowLayout();
    JTextField jTextServerName = new JTextField();
    JLabel jLabelSName = new JLabel();
    JLabel jLabel2 = new JLabel();

    public void setPlugin(Plugin plugin)
    {
        this.plugin = plugin;
    }

    /**Construct the frame*/
    public HelloWorldFrame()
    {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**Component initialization*/
    private void jbInit() throws Exception
    {
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(390, 65));
        this.setTitle("Hello World");
        jPanel1.setLayout(flowLayout1);
        jButtonSayHello.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                jButtonSayHello_actionPerformed(e);
            }
        });
        jTextServerName.setPreferredSize(new Dimension(125, 21));
        jTextServerName.setText("localhost:8082");
        jLabelSName.setText("Talk to Transceiver:");
        jLabel2.setText("          ");
        contentPane.add(jPanel1, BorderLayout.CENTER);
        jPanel1.add(jLabelSName, null);
        jPanel1.add(jTextServerName, null);
        jPanel1.add(jLabel2, null);
        jPanel1.add(jButtonSayHello, null);
        jButtonSayHello.setText("Say Hello");
    }

    // Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            // Shutdown the TransCeiver when the application is closed
            plugin.shutdown();
        }
    }

    // Called when the "SayHello" button is pressed
    void jButtonSayHello_actionPerformed(ActionEvent e)
    {
        ((HelloWorld)plugin).sayHelloButton(jTextServerName.getText());
   }
}