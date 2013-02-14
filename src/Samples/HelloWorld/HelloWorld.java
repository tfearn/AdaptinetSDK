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

import javax.swing.*;
import org.adaptinet.pluginagent.Plugin;
import org.adaptinet.messaging.*;

public class HelloWorld extends Plugin
{
    boolean packFrame = false;
    HelloWorldFrame frame = null;

    // This method is called by the transceiver for initialization
    public void init()
    {
        frame = new HelloWorldFrame();

        if (packFrame)
            frame.pack();
        else
            frame.validate();
        frame.setVisible(true);
        frame.setPlugin(this);

        // Set the title of the frame to include my hostname and port
        Address address = new Address(this.transceiver);
        frame.setTitle("Hello World - " + address.getHost() + ":" +
            address.getPort());
    }

    // This method is called by the transceiver for cleanup
    public void cleanup()
    {
    }

    // This method is called by the Frame when the Say Hello button
    // is pressed.
    public void sayHelloButton(String to)
    {
        // Create the argument list for the call to another transceiver
        Object[] args = new Object[1];

        // Get the Address for my local machine by passing in the
        // transceiver object
        Address address = new Address(this.transceiver);
        args[0] = "Hello, my name is " + address.getHost() + ":" +
            address.getPort() + ".";

        // The Message object defines who we are going to talk to.  The
        // URI format is as follows:
        //      http://ip-or-host-name:port/plugin/method
        Message message = new Message("http://" + to + "/HelloWorld/Hello",
            this.transceiver);

        try
        {
            // Go ahead and send the message
            postMessage(message, args);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    // This method is called by other transceivers running the
    // HelloWorld plugin.
    public void Hello(String text)
    {
        // Pop up a dialog showing the text sent to me
        JOptionPane.showMessageDialog(null, "[Message] " + text,
            "New Message Received", JOptionPane.INFORMATION_MESSAGE);
    }
}