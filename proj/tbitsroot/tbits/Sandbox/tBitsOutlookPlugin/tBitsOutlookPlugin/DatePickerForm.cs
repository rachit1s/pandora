using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace tBitsOutlookPlugin
{
    public partial class DatePicker : Form
    {
        public string date = null;
        public DatePicker()
        {
            InitializeComponent();            
        }        

        private void DatePickerForm_Load(object sender, EventArgs e)
        {            
        }        

        private void dateTimePicker1_ValueChanged(object sender, EventArgs e)
        {
            this.date = this.dateTimePicker1.Text;            
            this.Close();           
        }
    }
}