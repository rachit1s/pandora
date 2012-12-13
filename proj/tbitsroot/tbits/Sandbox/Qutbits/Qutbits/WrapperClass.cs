using System;
using System.Collections.Generic;
using System.Text;

namespace Qutbits
{
    // This is the abstract class used to wrap objects of any type
    // ------------------- WrapperClass.cs ----------------------------- 

    /// <summary>
    /// Delegate signature to inform the application about closed objects.
    /// </summary>
    /// <param name="id">The unique ID of the closed object.</param>
    public delegate void WrapperClosedDelegate(Guid id);

    /// <summary>
    /// The Wrapperclass itself has a unique ID and a closed event.
    /// </summary>
    internal abstract class WrapperClass
    {
        /// <summary>
        /// The event ocures when the monitored item has been closed.
        /// </summary>
        public event WrapperClosedDelegate Closed;

        protected static string currentId = null;
        protected static string previousId = null;

        /// <summary>
        /// The unique ID of the wrapped object.
        /// </summary>
        public Guid Id;//{ get; private set; }

        protected void OnClosed()
        {
            if (Closed != null)
            {
                Closed(Id);
                currentId = previousId;
            }
        }

        /// <summary>
        /// The constructor creates a new unique ID.
        /// </summary>
        public WrapperClass()
        {
            Id = Guid.NewGuid();
        }
    }
}
