package filipe.guerreiro.ui.home

import androidx.lifecycle.ViewModel
import filipe.guerreiro.data.local.dao.CashDao

class HomeViewModel(
    private val cashDao: CashDao
) : ViewModel() {

}